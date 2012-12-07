/*
 *  Copyright 2008 biaoping.yin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.frameworkset.persitent.util;

import java.lang.ref.SoftReference;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.frameworkset.common.poolman.sql.PoolManResultSetMetaData;
import com.frameworkset.util.VariableHandler;
import com.frameworkset.util.VariableHandler.SQLStruction;

/**
 * <p>Title: SQLCache.java</p> 
 * <p>Description: </p>
 * <p>bboss workgroup</p>
 * <p>Copyright (c) 2007</p>
 * @Date 2012-12-5 ����6:15:07
 * @author biaoping.yin
 * @version 1.0
 */
public class SQLCache {
	private Object lock = new Object();
	private Map<String,SQLStruction> parserSQLStructions = new java.util.WeakHashMap<String,SQLStruction>();
	private Map<String,SQLStruction> parsertotalsizeSQLStructions = new java.util.WeakHashMap<String,SQLStruction>();
	protected Map<String,Map<String, SoftReference<PoolManResultSetMetaData>>> metas = new HashMap<String,Map<String, SoftReference<PoolManResultSetMetaData>>>();
	public SQLCache() {
		// TODO Auto-generated constructor stub
	}
	
	
	public void clear()
	{
		metas.clear();
		parserSQLStructions.clear();
		parsertotalsizeSQLStructions.clear();
	}
	public PoolManResultSetMetaData getPoolManResultSetMetaData(String dbname,String sqlkey,ResultSetMetaData rsmetadata) throws SQLException
	{
		PoolManResultSetMetaData meta = null;
		Map<String, SoftReference<PoolManResultSetMetaData>> dbmetas = metas.get(dbname);
		if(dbmetas == null)
		{
			synchronized(metas)
			{
				dbmetas = metas.get(dbname);
				if(dbmetas == null)
				{
					dbmetas = new HashMap<String, SoftReference<PoolManResultSetMetaData>>();
					metas.put(dbname, dbmetas);
				}
			}
		}
		sqlkey = sqlkey + "__pagine" ;
		if (dbmetas.containsKey(sqlkey)) {
			SoftReference<PoolManResultSetMetaData> wr =  dbmetas.get(sqlkey);
			meta = (PoolManResultSetMetaData) wr.get();
			if (meta == null) {
				meta = PoolManResultSetMetaData.getCopy(rsmetadata);
				SoftReference<PoolManResultSetMetaData> wr1 = new SoftReference<PoolManResultSetMetaData>(meta);
				dbmetas.put(sqlkey, wr1);
			}
		} else {
			meta = PoolManResultSetMetaData.getCopy(rsmetadata);
			SoftReference<PoolManResultSetMetaData> wr = new SoftReference<PoolManResultSetMetaData>(meta);
			dbmetas.put(sqlkey, wr);
		}
		return meta;
	}
	
	public SQLStruction getSQLStruction(String sql)
	{
		SQLStruction sqlstruction =  parserSQLStructions.get(sql);
        if(sqlstruction == null)
        {
            synchronized(lock)
            {
            	sqlstruction =  parserSQLStructions.get(sql);
                if(sqlstruction == null)
                {
                	sqlstruction = VariableHandler.parserSQLStruction(sql);
                	parserSQLStructions.put(sql,sqlstruction);
                }
            }
        }  
        return sqlstruction;
	}
	
	public SQLStruction getTotalsizeSQLStruction(String totalsizesql)
	{
		SQLStruction totalsizesqlstruction =  parsertotalsizeSQLStructions.get(totalsizesql);
	    if(totalsizesqlstruction == null)
	    {
	        synchronized(lock)
	        {
	        	totalsizesqlstruction =  parsertotalsizeSQLStructions.get(totalsizesql);
	            if(totalsizesqlstruction == null)
	            {
	            	totalsizesqlstruction = VariableHandler.parserSQLStruction(totalsizesql);
	            	parsertotalsizeSQLStructions.put(totalsizesql,totalsizesqlstruction);
	            }
	        }
	    } 
        return totalsizesqlstruction;
	}

}
