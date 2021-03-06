/* This file is in the public domain. */

package slammer.analysis;

import java.text.DecimalFormat;
import slammer.*;
import java.math.*;

public class ImportRecords extends Analysis
{
	/* the algorithms/programs
	 *
	 * Note: the arias, dobry, redigitize, and rigorous slammer algorithms were
	 * originally written by Ray Wilson in GW Basic, and then ported to C++ and
	 * Java.
	 */

	// From: http://stackoverflow.com/a/2808648/864236
	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public static String Arias(DoubleList data, final double di)
	{
		return fmtThree.format(arias(data, di));
	}

	public static double arias(DoubleList data, final double di)
	{
		return round(AriasDobry(data, di, false), 3);
	}

	/* Arias, with optional dobry.  If the boolean dobry is true, this function
	 * will return the dobry duration as opposed to the arias intensity.
	 */
	private static double AriasDobry(DoubleList data, final double di, boolean dobry)
	{
		Double val;
		double d, u = 0, a, x, j, z, b, t, k = 0, res;
		d = di;
		j = .1603 * d * u;
		data.reset();
		while((val = data.each()) != null)
		{
			a = val.doubleValue();
			a /= 100.0;
			x = a * a;
			u += x;
		}
		j = .1603 * d * u;
		if(dobry)
		{	z = .95 * j / (.1603 * d);
			b = .05 * j / (.1603 * d);
			u = 0;
			data.reset();
			while((val = data.each()) != null)
			{
				a = val.doubleValue();
				a /= 100.0;
				x = a * a;
				u += x;
				if(u > z || u < b) k--;
				k++;
			}
			t = k * d;
			return t;
		}
		else
		{
			return j;
		}
	}

	public static String Dobry(DoubleList data, final double di)
	{
		return fmtOne.format(dobry(data, di));
	}

	public static double dobry(DoubleList data, final double di)
	{
		return round(AriasDobry(data, di, true), 1);
	}

	public static String PGA(DoubleList data)
	{
		return fmtThree.format(pga(data));
	}

	public static double pga(DoubleList data)
	{
		return round(FindMax(data) / Gcmss, 3); // store in g's, but expect to be in cm/s/s
	}

	public static String PGV(DoubleList data, final double di)
	{
		return fmtOne.format(pgv(data, di));
	}

	public static double pgv(DoubleList data, final double di)
	{
		Double val;
		double max, cur, curabs;

		max = 0;
		cur = 0;

		data.reset();
		while((val = data.each()) != null)
		{
			cur += val.doubleValue();
			curabs = Math.abs(cur);

			if(curabs > max)
				max = curabs;
		}

		return round(max * di, 1);
	}

	private static double FindMax(DoubleList data)
	{
		Double val;
		double here, max = 0;
		data.reset();
		while((val = data.each()) != null)
		{
			here = Math.abs(val.doubleValue());
			if(here > max) max = here;
		}
		return max;
	}

	public static String MeanPer(DoubleList data, final double di)
	{
		return fmtTwo.format(meanPer(data, di));
	}

	public static double meanPer(DoubleList data, final double di)
	{
		double[] arr = new double[data.size()];

		Double temp;
		data.reset();
		for(int i = 0; (temp = data.each()) != null; i++)
			arr[i] = temp.doubleValue();

		double[][] narr = fftWrap(arr, di);

		// set frequency increment
		double df = 1.0 / ((double)(narr.length) * di);

		double top = 0, bot = 0, top2 = 0, bot2 = 0, tms = 0, to = 0;
		double f;

		for(int i = 0; i < arr.length; i++)
		{
			arr[i] = Math.sqrt(Math.pow(narr[i][0], 2) + Math.pow(narr[i][1], 2));
			f = i * df;

			if(f > 0.25 && f < 20.0)
			{
				top += (1.0 / f) * Math.pow(arr[i], 2);
				bot += Math.pow(arr[i], 2);
				top2 += Math.pow(1.0 / f, 2) * Math.pow(arr[i], 2);
				bot2 += Math.pow(arr[i], 2);
			}
		}

		return round(top / bot, 2);
	}

	public static double[][] fftWrap(final double[] array, final double di)
	{
		rdc(array);
		taper(array);

		// Pads the array so its length is a power of 2.
		int test = 0;

		for(int i = 1; test < array.length; i++)
		{
			test = (int)Math.pow(2, i);
		}

		double[][] narr = new double[test][2];

		for(int i = 0; i < array.length; i++)
		{
			narr[i][0] = array[i];
			narr[i][1] = 0;
		}

		for(int i = narr.length; i < test; i++)
		{
			narr[i][0] = 0;
			narr[i][1] = 0;
		}

		// forward fft
		fft(narr);

		// scale to keep units correct
		for(int i = 0; i < narr.length; i++)
		{
			narr[i][0] *= di;
			narr[i][1] *= di;
		}

		return narr;
	}

	public static void fft(double[][] array)
	{
		double u_r,u_i, w_r,w_i, t_r,t_i;
		int ln, nv2, k, l, le, le1, j, ip, i, n;

		n = array.length;
		ln = (int)(Math.log((double)n) / Math.log(2) + 0.5);
		nv2 = n / 2;
		j = 1;

		for (i = 1; i < n; i++ )
		{
			if (i < j)
			{
				t_r = array[i - 1][0];
				t_i = array[i - 1][1];
				array[i - 1][0] = array[j - 1][0];
				array[i - 1][1] = array[j - 1][1];
				array[j - 1][0] = t_r;
				array[j - 1][1] = t_i;
			}

			k = nv2;

			while (k < j)
			{
				j = j - k;
				k = k / 2;
			}

			j = j + k;
		}

		for (l = 1; l <= ln; l++) /* loops thru stages */
		{
			le = (int)(Math.exp((double)l * Math.log(2)) + 0.5);
			le1 = le / 2;
			u_r = 1.0;
			u_i = 0.0;
			w_r =  Math.cos(Math.PI / (double)le1);
			w_i = -Math.sin(Math.PI / (double)le1);

			for (j = 1; j <= le1; j++) /* loops thru 1/2 twiddle values per stage */
			{
				for (i = j; i <= n; i += le) /* loops thru points per 1/2 twiddle */
				{
					ip = i + le1;
					t_r = array[ip - 1][0] * u_r - u_i * array[ip - 1][1];
					t_i = array[ip - 1][1] * u_r + u_i * array[ip - 1][0];

					array[ip - 1][0] = array[i - 1][0] - t_r;
					array[ip - 1][1] = array[i - 1][1] - t_i;

					array[i - 1][0] =  array[i - 1][0] + t_r;
					array[i - 1][1] =  array[i - 1][1] + t_i;
				}
				t_r = u_r * w_r - w_i * u_i;
				u_i = w_r * u_i + w_i * u_r;
				u_r = t_r;
			}
		}
	}

	// Removes a dc shift from the data by removing the mean.
	public static void rdc(double[] arr)
	{
		double sum = 0, mean;

		for(int i = 0; i < arr.length; i++)
			sum += arr[i];

		mean = sum / (double)(arr.length);

		for(int i = 0; i < arr.length; i++)
			arr[i] -= mean;
	}

	// Tapers the first and last 5% of the data.
	public static void taper(double[] arr)
	{
		double n, arg;
		double taper = 5;

		n = (double)(arr.length) * taper / 100.0;

		// beginning 5%
		for(int i = 0; i < n; i++)
		{
			arg = Math.PI * (double)(i) / (double)(n) + Math.PI;
			arr[i] *= (1.0 + Math.cos(arg)) / 2.0;
		}

		//ending 5%
		for(int i = 0; i < n; i++)
		{
			arg = Math.PI * (double)(i) / (double)(n) + Math.PI;
			arr[arr.length - i - 1] *= (1.0 + Math.cos(arg)) / 2.0;
		}
	}

	/* Compute maximum response
	 * Written by I.M. Idriss at U.C. Berkeley 1968, using the
	 * technique presented by Nigam and Jennigs (1969( in
	 * "Calculation of Response Spectra from String-Motion
	 * Earthquake Records": Bulletin of the Seismological Society
	 * of America, vol. 59, pp. 909-922.
	 *
	 * April Converse made minor changes in 1989.
	 * Matt Jibson ported to Java with additions in 2005.
	 *
	 * arr = time series data, cm/sec/sec
	 * w = oscillator natural frequency = (2*pi)/(oscillator period)
	 * d = damping, as fraction of critical damping
	 * dt = digitization interval
	 *
	 * Returns:
	 *       ZA = maximum absolute acceleration response, cm/sec/sec
	 *       ZV = maximum relative velocity     response, cm/sec
	 *       ZD = maximum relative displacement response, cm
	 * w^2 * ZD =  psuedo absolute acceleration response, cm/sec/sec
	 * w   * ZD =  psuedo relative velocity     response, cm/sec
	 */
	public static double[] cmpmax(double[] arr, double w, double d, double dt)
	{
		double w2 = w * w;
		double w3 = w2 * w;
		double wd = w * Math.sqrt(1.0 - d * d);

		double xd[] = {0, 0};
		double xv[] = {0, 0};

		double f1 = 2.0 * d / (w3 * dt);
		double f2 = 1.0 / w2;
		double f3 = d * w;
		double f4 = 1.0 / wd;
		double f5 = f3 * f4;
		double f6 = 2.0 * f3;
		double e = Math.exp(-f3 * dt);
		double s = Math.sin(wd * dt);
		double c = Math.cos(wd * dt);
		double g1 = e * s;
		double g2 = e * c;
		double h1 = wd * g2 - f3 * g1;
		double h2 = wd * g1 + f3 * g2;
		double dug, z1, z2, z3, z4, a, b, aa, f, g, h, zd = 0, zv = 0, za = 0;

		for(int k = 0; k < (arr.length - 1); k++)
		{
			dug = arr[k + 1] - arr[k];
			z1 = f2 * dug;
			z2 = f2 * arr[k];
			z3 = f1 * dug;
			z4 = z1 / dt;
			b = xd[0] + z2 - z3;
			a = f4 * xv[0] + f5 * b + f4 * z4;
			xd[1] = a * g1 + b * g2 + z3 - z2 - z1;
			xv[1] = a * h1 - b * h2 - z4;
			xd[0] = xd[1];
			xv[0] = xv[1];
			aa = -f6 * xv[0] - w2 * xd[0];
			f = Math.abs(xd[0]);
			g = Math.abs(xv[0]);
			h = Math.abs(aa);
			if(f > zd) zd = f;
			if(g > zv) zv = g;
			if(h > za) za = h;
		}

		return new double[] {za, zv, zd, w2 * zd, w * zd};
	}
}
