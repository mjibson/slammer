/* This file is in the public domain. */

package slammer;

import java.sql.*;
import javax.swing.*;
import java.util.ArrayList;
import slammer.gui.*;

public class Utils
{
	public static EQDatabase db = null;
	private static ArrayList eqVec = new ArrayList();
	private static ArrayList eqMan = new ArrayList();

	private static boolean locked = false;

	public static void startDB() throws Exception
	{
		if(db == null)
			db = new EQDatabase();
	}

	public static EQDatabase getDB() throws Exception
	{
		if(db == null)
			startDB();

		return db;
	}

	public static void closeDB() throws Exception
	{
		if(db != null)
		{
			db.close();
			db = null;
		}
	}

	public static void catchException(Throwable ex)
	{
		StackTraceElement e[] = ex.getStackTrace();
		String trace = "";

		for(int i = 0; i < e.length; i++)
		{
			trace = trace + e[i].getClassName() + "(" + e[i].getFileName() + ":" + e[i].getLineNumber() + ")\n";
		}

		GUIUtils.popupError("Error: " + ex.getMessage() + "\n" + trace);
	}

	public static Object checkNum(String str, String label, Double lt, boolean lte, String lts, Double gt, boolean gte, String gts, boolean ret)
	{
		Object o = checkNum(str, label, lt, lte, lts, gt, gte, gts);

		if(ret)
		{
			return o;
		}
		else
		{
			if(o.getClass().getName().equals("java.lang.Double"))
			{
				return o;
			}
			else
			{
				GUIUtils.popupError(o.toString());
				return null;
			}
		}
	}

	private static Object checkNum(String str, String label, Double lt, boolean lte, String lts, Double gt, boolean gte, String gts)
	{
		Double d;

		try
		{
			d = Double.valueOf(str);
		}
		catch (NumberFormatException e)
		{
			return ("Error: non-existent number (\"" + str + "\") in " + label + ".");
		}

		double dd = d.doubleValue();
		double comp;
		String message = "Error: " + str + " (" + label + ") must be";
		if(lt != null)
		{
			message += " ";
			message += "less than ";
			if(lte)
				message += "or equal to ";
			message += lt;
			if(lts != null && !lts.equals(""))
			message += " (" + lts + ")";
		}
		if(gt != null)
		{
			message += " ";
			if(lt != null)
				message += "and ";
			message += "greater than ";
			if(gte)
				message += "or equal to ";
			message += gt;
			if(gts != null && !gts.equals(""))
				message += " (" + gts + ")";
		}
		message += ".";


		if(lt != null)
		{
			comp = lt.doubleValue();
			if(lte)
			{
				if(comp < dd)
				{
					return message;
				}
			}
			else
			{
				if(comp <= dd)
				{
					return message;
				}
			}
		}
		if(gt != null)
		{
			comp = gt.doubleValue();
			if(gte)
			{
				if(comp > dd)
				{
					return message;
				}
			}
			else
			{
				if(comp >= dd)
				{
					return message;
				}
			}
		}
		return d;
	}

	public static void addEQList(JComboBox eqList) throws Exception
	{
		addEQList(eqList, Boolean.FALSE);
	}

	public static void addEQList(JComboBox eqList, Boolean manager) throws Exception
	{
		if(eqVec.contains(eqList) == false)
		{
			eqVec.add(eqList);
			eqMan.add(manager);
			setEQList(eqList, manager);
		}
	}

	public static void setEQList(JComboBox eqList, Boolean manager) throws Exception
	{
		boolean man = (manager == Boolean.TRUE) ? true : false;

		eqList.removeAllItems();

		if(man)
			eqList.addItem("All earthquakes");

		String[] list = getDB().getEQList();
		for(int i = 0; i < list.length; i++)
			eqList.addItem(list[i]);

		if(man)
		{
			Object[][] names = Utils.getDB().runQuery("select distinct name from grp order by name");
			if(names != null)
			{
				eqList.addItem(" -- Groups -- ");
				for(int i = 1; i < names.length; i++)
					eqList.addItem(names[i][0]);
			}
		}
	}

	public static void updateEQLists() throws Exception
	{
		lock();
		for(int i = 0; i < eqVec.size(); i++)
			setEQList((JComboBox)eqVec.get(i), (Boolean)eqMan.get(i));
		unlock();
	}

	public static void updateRecordList(JComboBox recordList, String eq) throws Exception
	{
		recordList.removeAllItems();

		recordList.addItem("Select all records");
		String[] list = getDB().getRecordList(eq);
		for(int i = 0; i < list.length; i++)
			recordList.addItem(list[i]);
	}

	public static void updateRecordList(JComboBox recordList, JComboBox eqList) throws Exception
	{
		recordList.removeAllItems();

		if(eqList.getItemCount() == 0)
			return;

		recordList.addItem("Select all records");
		String[] list = getDB().getRecordList(eqList.getSelectedItem().toString());
		for(int i = 0; i < list.length; i++)
			recordList.addItem(list[i]);
	}

	public static String makeDefault(String s, String ifBlank)
	{
		if(s == null || s.equals(""))
			return ifBlank;

		return s;
	}

	public static boolean locked()
	{
		return locked;
	}

	public static void lock()
	{
		locked = true;
	}

	public static void unlock()
	{
		locked = false;
	}

	public static String shorten(Object s)
	{
		if(s == null)
			return "";
		return s.toString();
	}
}
