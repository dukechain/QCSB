/**
 * Copyright (c) 2010 Yahoo! Inc. All rights reserved. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */

package com.yahoo.ycsb.workloads;

import java.io.File;
import java.util.Properties;
import com.yahoo.ycsb.*;
import com.yahoo.ycsb.generator.CounterGenerator;
import com.yahoo.ycsb.generator.DiscreteGenerator;
import com.yahoo.ycsb.generator.Generator;
import com.yahoo.ycsb.generator.IntegerGenerator;
import com.yahoo.ycsb.generator.ScrambledZipfianGenerator;
import com.yahoo.ycsb.generator.SkewedLatestGenerator;
import com.yahoo.ycsb.generator.UniformIntegerGenerator;
import com.yahoo.ycsb.generator.ZipfianGenerator;
import com.yahoo.ycsb.measurements.Measurements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;

import org.apache.cassandra.db.SchedulerParameter;





/**
 * The core benchmark scenario. Represents a set of clients doing simple CRUD operations. The relative 
 * proportion of different kinds of operations, and other properties of the workload, are controlled
 * by parameters specified at runtime.
 * 
 * Properties to control the client:
 * <UL>
 * <LI><b>fieldcount</b>: the number of fields in a record (default: 10)
 * <LI><b>fieldlength</b>: the size of each field (default: 100)
 * <LI><b>readallfields</b>: should reads read all fields (true) or just one (false) (default: true)
 * <LI><b>writeallfields</b>: should updates and read/modify/writes update all fields (true) or just one (false) (default: false)
 * <LI><b>readproportion</b>: what proportion of operations should be reads (default: 0.95)
 * <LI><b>updateproportion</b>: what proportion of operations should be updates (default: 0.05)
 * <LI><b>insertproportion</b>: what proportion of operations should be inserts (default: 0)
 * <LI><b>scanproportion</b>: what proportion of operations should be scans (default: 0)
 * <LI><b>readmodifywriteproportion</b>: what proportion of operations should be read a record, modify it, write it back (default: 0)
 * <LI><b>requestdistribution</b>: what distribution should be used to select the records to operate on - uniform, zipfian or latest (default: uniform)
 * <LI><b>maxscanlength</b>: for scans, what is the maximum number of records to scan (default: 1000)
 * <LI><b>scanlengthdistribution</b>: for scans, what distribution should be used to choose the number of records to scan, for each scan, between 1 and maxscanlength (default: uniform)
 * <LI><b>insertorder</b>: should records be inserted in order by key ("ordered"), or in hashed order ("hashed") (default: hashed)
 * </ul> 
 */
public class CoreWorkload extends Workload
{

	/**
	 * The name of the database table to run queries against.
	 */
	public static final String TABLENAME_PROPERTY="table";

	/**
	 * The default name of the database table to run queries against.
	 */
	public static final String TABLENAME_PROPERTY_DEFAULT="usertable";

	public static String table;


	/**
	 * The name of the property for the number of fields in a record.
	 */
	public static final String FIELD_COUNT_PROPERTY="fieldcount";
	
	/**
	 * Default number of fields in a record.
	 */
	public static final String FIELD_COUNT_PROPERTY_DEFAULT="10";

	int fieldcount;

	/**
	 * The name of the property for the length of a field in bytes.
	 */
	public static final String FIELD_LENGTH_PROPERTY="fieldlength";
	
	/**
	 * The default length of a field in bytes.
	 */
	public static final String FIELD_LENGTH_PROPERTY_DEFAULT="100";

	int fieldlength;

	/**
	 * The name of the property for deciding whether to read one field (false) or all fields (true) of a record.
	 */
	public static final String READ_ALL_FIELDS_PROPERTY="readallfields";
	
	/**
	 * The default value for the readallfields property.
	 */
	public static final String READ_ALL_FIELDS_PROPERTY_DEFAULT="true";

	boolean readallfields;

	/**
	 * The name of the property for deciding whether to write one field (false) or all fields (true) of a record.
	 */
	public static final String WRITE_ALL_FIELDS_PROPERTY="writeallfields";
	
	/**
	 * The default value for the writeallfields property.
	 */
	public static final String WRITE_ALL_FIELDS_PROPERTY_DEFAULT="false";

	boolean writeallfields;


	/**
	 * The name of the property for the proportion of transactions that are reads.
	 */
	public static final String READ_PROPORTION_PROPERTY="readproportion";
	
	/**
	 * The default proportion of transactions that are reads.	
	 */
	public static final String READ_PROPORTION_PROPERTY_DEFAULT="0.95";

	/**
	 * The name of the property for the proportion of transactions that are updates.
	 */
	public static final String UPDATE_PROPORTION_PROPERTY="updateproportion";
	
	/**
	 * The default proportion of transactions that are updates.
	 */
	public static final String UPDATE_PROPORTION_PROPERTY_DEFAULT="0.05";

	/**
	 * The name of the property for the proportion of transactions that are inserts.
	 */
	public static final String INSERT_PROPORTION_PROPERTY="insertproportion";
	
	/**
	 * The default proportion of transactions that are inserts.
	 */
	public static final String INSERT_PROPORTION_PROPERTY_DEFAULT="0.0";

	/**
	 * The name of the property for the proportion of transactions that are scans.
	 */
	public static final String SCAN_PROPORTION_PROPERTY="scanproportion";
	
	/**
	 * The default proportion of transactions that are scans.
	 */
	public static final String SCAN_PROPORTION_PROPERTY_DEFAULT="0.0";
	
	/**
	 * The name of the property for the proportion of transactions that are read-modify-write.
	 */
	public static final String READMODIFYWRITE_PROPORTION_PROPERTY="readmodifywriteproportion";
	
	/**
	 * The default proportion of transactions that are scans.
	 */
	public static final String READMODIFYWRITE_PROPORTION_PROPERTY_DEFAULT="0.0";
	
	/**
	 * The name of the property for the the distribution of requests across the keyspace. Options are "uniform", "zipfian" and "latest"
	 */
	public static final String REQUEST_DISTRIBUTION_PROPERTY="requestdistribution";
	
	/**
	 * The default distribution of requests across the keyspace
	 */
	public static final String REQUEST_DISTRIBUTION_PROPERTY_DEFAULT="uniform";

	/**
	 * The name of the property for the max scan length (number of records)
	 */
	public static final String MAX_SCAN_LENGTH_PROPERTY="maxscanlength";
	
	/**
	 * The default max scan length.
	 */
	public static final String MAX_SCAN_LENGTH_PROPERTY_DEFAULT="1000";
	
	/**
	 * The name of the property for the scan length distribution. Options are "uniform" and "zipfian" (favoring short scans)
	 */
	public static final String SCAN_LENGTH_DISTRIBUTION_PROPERTY="scanlengthdistribution";
	
	/**
	 * The default max scan length.
	 */
	public static final String SCAN_LENGTH_DISTRIBUTION_PROPERTY_DEFAULT="uniform";
	
	/**
	 * The name of the property for the order to insert records. Options are "ordered" or "hashed"
	 */
	public static final String INSERT_ORDER_PROPERTY="insertorder";
	
	/**
	 * Default insert order.
	 */
	public static final String INSERT_ORDER_PROPERTY_DEFAULT="hashed";
	
	/**
	 * chen add
	 */
	public static final String WORKLOAD_PATH_PROPERTY="workloadpath";
	
	public static final String WORKLOAD_PATH_PROPERTY_DEFAULT="./workload.txt";
	
	public static final String tardiness_bound_PROPERTY="tardiness_bound";
	
	public static final String tardiness_bound_PROPERTY_DEFAULT="100";
	
	public static final String staleness_bound_PROPERTY="staleness_bound";
    
    public static final String staleness_bound_PROPERTY_DEFAULT="100";
    
    public static final String low_bound_QoS_preference_PROPERTY="low_preference";
    
    public static final String low_bound_QoS_preference_PROPERTY_DEFAULT="0.5";
    
    public static final String high_bound_QoS_preference_PROPERTY="high_preference";
    
    public static final String high_bound_QoS_preference_PROPERTY_DEFAULT="0.5";
    
    public static final String low_bound_query_weight_PROPERTY="low_weight";
    
    public static final String low_bound_query_weight_PROPERTY_DEFAULT="1";
    
    public static final String high_bound_query_weight_PROPERTY="high_weight";
    
    public static final String high_bound_query_weight_PROPERTY_DEFAULT="1";
    
	
	IntegerGenerator keysequence;

	DiscreteGenerator operationchooser;

	IntegerGenerator keychooser;

	Generator fieldchooser;

	CounterGenerator transactioninsertkeysequence;
	
	IntegerGenerator scanlength;
	
	boolean orderedinserts;

	int recordcount;
	
	
	//chen add
	boolean oldworkload = false;
	
	String workloadpath = null;
	
	
	int tardiness_bound;
	int staleness_bound;
	
	double low_bound_QoS_preference;
	double high_bound_QoS_preference;
	
	int low_bound_query_weight;
	int high_bound_query_weight;
	
	BlockingQueue<String> workloadhistories;
	
	
	class OperationLog
    {
        public String _name;
        public String _key;
        public HashSet<String> _field;
        public String _payload;
        
        private final String token = "\t";
        private final String fieldtoken = "|";

        OperationLog(String _name, String _key, HashSet<String> _field, String _payload)
        {
            this._name = _name;
            this._key = _key;
            this._field = _field;
            this._payload = _payload;
        }
        
        OperationLog(String str)
        {
            StringTokenizer tokenizer = new StringTokenizer(str, token);
            
            _name = tokenizer.nextToken();
            _key = tokenizer.nextToken();
            
            String field = tokenizer.nextToken();
            
            if (field.equals("ALLfields"))
            {
                _field = null;
            }
            else {
                _field = new HashSet<String>();
                
                StringTokenizer fieldTokenizer = new StringTokenizer(field, fieldtoken);
                while (fieldTokenizer.hasMoreTokens())
                {
                    String f = fieldTokenizer.nextToken();
                    
                    _field.add(f);
                    
                }
            }
            
            
            if (tokenizer.hasMoreTokens())
            {
                _payload = tokenizer.nextToken();
            }
            else {
                _payload = null;
            }
        }
        
        public String toString()
        {
            StringBuffer sb = new StringBuffer();
            
            sb.append(_name);
            sb.append(token);
            
            sb.append(_key);
            sb.append(token);
            
            if (_field != null)
            {
                for (String str : _field)
                {
                    sb.append(str);
                    sb.append(fieldtoken);
                }
            }
            else {
                sb.append("ALLfields");
            }
            sb.append(token);
            
            sb.append(_payload);
            
            return sb.toString();
        }
        
        
    }
	
	/**
	 * Initialize the scenario. 
	 * Called once, in the main client thread, before any operations are started.
	 */
	public void init(Properties p) throws WorkloadException
	{
		table = p.getProperty(TABLENAME_PROPERTY,TABLENAME_PROPERTY_DEFAULT);
		fieldcount=Integer.parseInt(p.getProperty(FIELD_COUNT_PROPERTY,FIELD_COUNT_PROPERTY_DEFAULT));
		fieldlength=Integer.parseInt(p.getProperty(FIELD_LENGTH_PROPERTY,FIELD_LENGTH_PROPERTY_DEFAULT));
		double readproportion=Double.parseDouble(p.getProperty(READ_PROPORTION_PROPERTY,READ_PROPORTION_PROPERTY_DEFAULT));
		double updateproportion=Double.parseDouble(p.getProperty(UPDATE_PROPORTION_PROPERTY,UPDATE_PROPORTION_PROPERTY_DEFAULT));
		double insertproportion=Double.parseDouble(p.getProperty(INSERT_PROPORTION_PROPERTY,INSERT_PROPORTION_PROPERTY_DEFAULT));
		double scanproportion=Double.parseDouble(p.getProperty(SCAN_PROPORTION_PROPERTY,SCAN_PROPORTION_PROPERTY_DEFAULT));
		double readmodifywriteproportion=Double.parseDouble(p.getProperty(READMODIFYWRITE_PROPORTION_PROPERTY,READMODIFYWRITE_PROPORTION_PROPERTY_DEFAULT));
		recordcount=Integer.parseInt(p.getProperty(Client.RECORD_COUNT_PROPERTY));
		String requestdistrib=p.getProperty(REQUEST_DISTRIBUTION_PROPERTY,REQUEST_DISTRIBUTION_PROPERTY_DEFAULT);
		int maxscanlength=Integer.parseInt(p.getProperty(MAX_SCAN_LENGTH_PROPERTY,MAX_SCAN_LENGTH_PROPERTY_DEFAULT));
		String scanlengthdistrib=p.getProperty(SCAN_LENGTH_DISTRIBUTION_PROPERTY,SCAN_LENGTH_DISTRIBUTION_PROPERTY_DEFAULT);
		
		int insertstart=Integer.parseInt(p.getProperty(INSERT_START_PROPERTY,INSERT_START_PROPERTY_DEFAULT));
		
		readallfields=Boolean.parseBoolean(p.getProperty(READ_ALL_FIELDS_PROPERTY,READ_ALL_FIELDS_PROPERTY_DEFAULT));
		writeallfields=Boolean.parseBoolean(p.getProperty(WRITE_ALL_FIELDS_PROPERTY,WRITE_ALL_FIELDS_PROPERTY_DEFAULT));
		
		/**
		 * chen add
		 */
		tardiness_bound=Integer.parseInt(p.getProperty(tardiness_bound_PROPERTY,tardiness_bound_PROPERTY_DEFAULT));
		staleness_bound=Integer.parseInt(p.getProperty(staleness_bound_PROPERTY, staleness_bound_PROPERTY_DEFAULT));
		
		low_bound_QoS_preference=Double.parseDouble(p.getProperty(low_bound_QoS_preference_PROPERTY, low_bound_QoS_preference_PROPERTY_DEFAULT));
		high_bound_QoS_preference=Double.parseDouble(p.getProperty(high_bound_QoS_preference_PROPERTY, high_bound_QoS_preference_PROPERTY_DEFAULT));
		
		low_bound_query_weight=Integer.parseInt(p.getProperty(low_bound_query_weight_PROPERTY, low_bound_query_weight_PROPERTY_DEFAULT));
		high_bound_query_weight=Integer.parseInt(p.getProperty(high_bound_query_weight_PROPERTY, high_bound_query_weight_PROPERTY_DEFAULT));
		
		workloadpath = p.getProperty(WORKLOAD_PATH_PROPERTY);
		if (workloadpath!=null)
        {
		    oldworkload = true;
	        workloadhistories = Utils.Reader(new File(workloadpath));

        }
		else {
		    oldworkload = false;
		    p.setProperty(WORKLOAD_PATH_PROPERTY, WORKLOAD_PATH_PROPERTY_DEFAULT);
		    
		    workloadpath = WORKLOAD_PATH_PROPERTY_DEFAULT;
		    
		    File file = new File(workloadpath);
		    if (file.exists())
            {
                file.delete();
            }
        }
		
		/**
		 * 
		 */
		
		
		if (p.getProperty(INSERT_ORDER_PROPERTY,INSERT_ORDER_PROPERTY_DEFAULT).compareTo("hashed")==0)
		{
			orderedinserts=false;
		}
		else
		{
			orderedinserts=true;
		}

		keysequence=new CounterGenerator(insertstart);
		operationchooser=new DiscreteGenerator();
		if (readproportion>0)
		{
			operationchooser.addValue(readproportion,"READ");
		}

		if (updateproportion>0)
		{
			operationchooser.addValue(updateproportion,"UPDATE");
		}

		if (insertproportion>0)
		{
			operationchooser.addValue(insertproportion,"INSERT");
		}
		
		if (scanproportion>0)
		{
			operationchooser.addValue(scanproportion,"SCAN");
		}
		
		if (readmodifywriteproportion>0)
		{
			operationchooser.addValue(readmodifywriteproportion,"READMODIFYWRITE");
		}

		transactioninsertkeysequence=new CounterGenerator(recordcount);
		if (requestdistrib.compareTo("uniform")==0)
		{
			keychooser=new UniformIntegerGenerator(0,recordcount-1);
		}
		else if (requestdistrib.compareTo("zipfian")==0)
		{
			//it does this by generating a random "next key" in part by taking the modulus over the number of keys
			//if the number of keys changes, this would shift the modulus, and we don't want that to change which keys are popular
			//so we'll actually construct the scrambled zipfian generator with a keyspace that is larger than exists at the beginning
			//of the test. that is, we'll predict the number of inserts, and tell the scrambled zipfian generator the number of existing keys
			//plus the number of predicted keys as the total keyspace. then, if the generator picks a key that hasn't been inserted yet, will
			//just ignore it and pick another key. this way, the size of the keyspace doesn't change from the perspective of the scrambled zipfian generator
			
			int opcount=Integer.parseInt(p.getProperty(Client.OPERATION_COUNT_PROPERTY));
			int expectednewkeys=(int)(((double)opcount)*insertproportion*2.0); //2 is fudge factor
			
			keychooser=new ScrambledZipfianGenerator(recordcount+expectednewkeys);
		}
		else if (requestdistrib.compareTo("latest")==0)
		{
			keychooser=new SkewedLatestGenerator(transactioninsertkeysequence);
		}
		else
		{
			throw new WorkloadException("Unknown distribution \""+requestdistrib+"\"");
		}

		fieldchooser=new UniformIntegerGenerator(0,fieldcount-1);
		
		if (scanlengthdistrib.compareTo("uniform")==0)
		{
			scanlength=new UniformIntegerGenerator(1,maxscanlength);
		}
		else if (scanlengthdistrib.compareTo("zipfian")==0)
		{
			scanlength=new ZipfianGenerator(1,maxscanlength);
		}
		else
		{
			throw new WorkloadException("Distribution \""+scanlengthdistrib+"\" not allowed for scan length");
		}

	}
	
	
	public void writeOperation(OperationLog op)
	{
	    Utils.writeLineAppend(workloadpath, op.toString());
	}

	/**
	 * Do one insert operation. Because it will be called concurrently from multiple client threads, this 
	 * function must be thread safe. However, avoid synchronized, or the threads will block waiting for each 
	 * other, and it will be difficult to reach the target throughput. Ideally, this function would have no side
	 * effects other than DB operations.
	 */
	public boolean doInsert(DB db, Object threadstate)
	{
	    if (oldworkload)
        {
            return doInsert(db);
        }
	    
		int keynum=keysequence.nextInt();
		if (!orderedinserts)
		{
			keynum=Utils.hash(keynum);
		}
		String dbkey="user"+keynum;
		HashMap<String,String> values=new HashMap<String,String>();
		
		HashSet<String> fields = new HashSet<String>();
		
		int field_count = new UniformIntegerGenerator(1, fieldcount).nextInt();
		
		for (int i=0; i<field_count; i++)
        {
		    //int field_length = new UniformIntegerGenerator(1, fieldlength).nextInt();
		    
            String fieldkey="field"+i;
            String data=Utils.ASCIIString(fieldlength);
            values.put(fieldkey,data);
            
            fields.add(fieldkey);
        }
		
		/*for (int i=0; i<fieldcount; i++)
		{
			String fieldkey="field"+i;
			String data=Utils.ASCIIString(fieldlength);
			values.put(fieldkey,data);
			
			fields.add(fieldkey);
		}*/
		
		
		OperationLog oplog = new OperationLog("LOAD", dbkey, fields, "");
        
        writeOperation(oplog);
		
		if (db.insert(table,dbkey,values) == 0)
			return true;
		else
			return false;
	}
	
	public boolean doInsert(DB db)
    {
	    if (workloadhistories.isEmpty())
        {
            return true;
        }
        
        OperationLog operation = null;
        try
        {
            operation = new OperationLog(workloadhistories.take());
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
	    
	   
        String dbkey=operation._key;
        HashMap<String,String> values=new HashMap<String,String>();
        
        for (String fieldname : operation._field)
        {
            String data=Utils.ASCIIString(fieldlength);         
            values.put(fieldname,data);
        }

        if (db.insert(table,dbkey,values) == 0)
            return true;
        else
            return false;
    }

	/**
	 * Do one transaction operation. Because it will be called concurrently from multiple client threads, this 
	 * function must be thread safe. However, avoid synchronized, or the threads will block waiting for each 
	 * other, and it will be difficult to reach the target throughput. Ideally, this function would have no side
	 * effects other than DB operations.
	 */
	public boolean doTransaction(DB db, Object threadstate)
	{
	    /**
	     * chen add
	     */
	    if (oldworkload)
        {
            return doTransaction(db);
        }
	    
		String op=operationchooser.nextString();

		if (op.compareTo("READ")==0)
		{
			doTransactionRead(db);
		}
		else if (op.compareTo("UPDATE")==0)
		{
			doTransactionUpdate(db);
		}
		else if (op.compareTo("INSERT")==0)
		{
			doTransactionInsert(db);
		}
		else if (op.compareTo("SCAN")==0)
		{
			doTransactionScan(db);
		}
		else
		{
			doTransactionReadModifyWrite(db);
		}
		
		return true;
	}
	
	public boolean doTransaction(DB db)
    {
	    if (workloadhistories.isEmpty())
        {
            return true;
        }
	    
	    OperationLog operation = null;
        try
        {
            operation = new OperationLog(workloadhistories.take());
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        
        String op = operation._name;

        if (op.compareTo("READ")==0)
        {
            doTransactionRead(db, operation);
        }
        else if (op.compareTo("UPDATE")==0)
        {
            doTransactionUpdate(db, operation);
        }
        else if (op.compareTo("INSERT")==0)
        {
            doTransactionInsert(db, operation);
        }
        else if (op.compareTo("SCAN")==0)
        {
            doTransactionScan(db);
        }
        else
        {
            doTransactionReadModifyWrite(db);
        }
        
        return true;
    }

    public void doTransactionRead(DB db, OperationLog operation)
    {
        String keyname=operation._key;

        HashSet<String> fields=operation._field;
        
        HashMap<String, String> pa = new HashMap<String, String>();
        pa.put("para", operation._payload.toString());

        //db.read(table,keyname,fields,new HashMap<String,String>());   
        
        db.read(table,keyname,fields,pa);   
    }

    public void doTransactionRead(DB db)
	{
		//choose a random key
		int keynum;
		do
		{
			keynum=keychooser.nextInt();
		}
		while (keynum>transactioninsertkeysequence.lastInt());
		
		if (!orderedinserts)
		{
			keynum=Utils.hash(keynum);
		}
		String keyname="user"+keynum;

		HashSet<String> fields=null;

		if (!readallfields)
		{
			//read a random field  
			String fieldname="field"+fieldchooser.nextString();

			fields=new HashSet<String>();
			fields.add(fieldname);
		}
		
		SchedulerParameter paras = new SchedulerParameter();
		
		paras.tardiness_deadline = new UniformIntegerGenerator(0, tardiness_bound).nextInt();
		
		paras.staleness_deadline = new UniformIntegerGenerator(0, staleness_bound).nextInt();
		
		paras.QoS_preference = new UniformIntegerGenerator((int)(low_bound_QoS_preference*10d), 
		        (int)(high_bound_QoS_preference*10d)).nextInt()/10d;
		
		paras.query_weight = new UniformIntegerGenerator(low_bound_query_weight, 
		        high_bound_query_weight).nextInt();

		OperationLog oplog = new OperationLog("READ", keyname, fields, paras.toString());
		
		writeOperation(oplog);
		
		//db.read(table,keyname,fields,new HashMap<String,String>());
		HashMap<String, String> pa = new HashMap<String, String>();
		pa.put("para", paras.toString());
		db.read(table,keyname,fields,pa);
	}
	
	public void doTransactionReadModifyWrite(DB db)
	{
		//choose a random key
		int keynum;
		do
		{
			keynum=keychooser.nextInt();
		}
		while (keynum>transactioninsertkeysequence.lastInt());
		
		if (!orderedinserts)
		{
			keynum=Utils.hash(keynum);
		}
		String keyname="user"+keynum;

		HashSet<String> fields=null;

		if (!readallfields)
		{
			//read a random field  
			String fieldname="field"+fieldchooser.nextString();

			fields=new HashSet<String>();
			fields.add(fieldname);
		}
		
		HashMap<String,String> values=new HashMap<String,String>();

		if (writeallfields)
		{
		   //new data for all the fields
		   for (int i=0; i<fieldcount; i++)
		   {
		      String fieldname="field"+i;
		      String data=Utils.ASCIIString(fieldlength);		   
		      values.put(fieldname,data);
		   }
		}
		else
		{
		   //update a random field
		   String fieldname="field"+fieldchooser.nextString();
		   String data=Utils.ASCIIString(fieldlength);		   
		   values.put(fieldname,data);
		}

		//do the transaction
		
		long st=System.currentTimeMillis();

		db.read(table,keyname,fields,new HashMap<String,String>());
		
		db.update(table,keyname,values);

		long en=System.currentTimeMillis();
		
		Measurements.getMeasurements().measure("READ-MODIFY-WRITE", (int)(en-st));
	}
	
	public void doTransactionScan(DB db)
	{
		//choose a random key
		int keynum;
		do
		{
			keynum=keychooser.nextInt();
		}
		while (keynum>transactioninsertkeysequence.lastInt());

		if (!orderedinserts)
		{
			keynum=Utils.hash(keynum);
		}
		String startkeyname="user"+keynum;
		
		//choose a random scan length
		int len=scanlength.nextInt();

		HashSet<String> fields=null;

		if (!readallfields)
		{
			//read a random field  
			String fieldname="field"+fieldchooser.nextString();

			fields=new HashSet<String>();
			fields.add(fieldname);
		}

		db.scan(table,startkeyname,len,fields,new Vector<HashMap<String,String>>());
	}
	
	private void doTransactionUpdate(DB db, OperationLog operation)
    {
	     //choose a random key
	    String keyname=operation._key;

        HashSet<String> fields=operation._field;

        HashMap<String,String> values=new HashMap<String,String>();

        if (writeallfields)
        {
           //new data for all the fields
           for (int i=0; i<fieldcount; i++)
           {
              String fieldname="field"+i;
              String data=Utils.ASCIIString(fieldlength);          
              values.put(fieldname,data);
           }
        }
        else
        {
           //update a random field
           //String fieldname="field"+fieldchooser.nextString();
           
           for (String fieldname : fields)
           {
               String data=Utils.ASCIIString(fieldlength);         
               values.put(fieldname,data);
           }
        }
        
        
        db.update(table,keyname,values);
        
    }

	public void doTransactionUpdate(DB db)
	{
		//choose a random key
		int keynum;
		do
		{
			keynum=keychooser.nextInt();
		}
		while (keynum>transactioninsertkeysequence.lastInt());

		if (!orderedinserts)
		{
			keynum=Utils.hash(keynum);
		}
		String keyname="user"+keynum;

		HashMap<String,String> values=new HashMap<String,String>();

		
		HashSet<String> fields = new HashSet<String>();
		if (writeallfields)
		{
		   //new data for all the fields
		   for (int i=0; i<fieldcount; i++)
		   {
		      String fieldname="field"+i;
		      String data=Utils.ASCIIString(fieldlength);		   
		      values.put(fieldname,data);
		   }
		   
		   //chen add
		   fields = null;
		}
		else
		{
		   
		   //update a random field
		   String fieldname="field"+fieldchooser.nextString();
		   String data=Utils.ASCIIString(fieldlength);		   
		   values.put(fieldname,data);
		   
		   //chen
		   fields.add(fieldname);
		}
		
		//chen
		OperationLog oplog = new OperationLog("UPDATE", keyname, fields, "");
        
        writeOperation(oplog);


		db.update(table,keyname,values);
	}

	public void doTransactionInsert(DB db)
	{
		//choose the next key
		int keynum=transactioninsertkeysequence.nextInt();
		if (!orderedinserts)
		{
			keynum=Utils.hash(keynum);
		}
		String dbkey="user"+keynum;
		
		HashMap<String,String> values=new HashMap<String,String>();
		
		HashSet<String> fields = new HashSet<String>();
		

		int field_count = new UniformIntegerGenerator(1, fieldcount).nextInt();
        
        for (int i=0; i<field_count; i++)
        {
            //int field_length = new UniformIntegerGenerator(1, fieldlength).nextInt();
            
            String fieldkey="field"+i;
            String data=Utils.ASCIIString(fieldlength);
            values.put(fieldkey,data);
            
            fields.add(fieldkey);
        }
		
		/*for (int i=0; i<fieldcount; i++)
		{
			String fieldkey="field"+i;
			String data=Utils.ASCIIString(fieldlength);
			values.put(fieldkey,data);
			
			fields.add(fieldkey);
		}*/
		
		//chen
        OperationLog oplog = new OperationLog("INSERT", dbkey, fields, "");
        
        writeOperation(oplog);
		
		
		db.insert(table,dbkey,values);
	}
	
	public void doTransactionInsert(DB db, OperationLog operation)
    {
        //choose the next key
       /* int keynum=transactioninsertkeysequence.nextInt();
        if (!orderedinserts)
        {
            keynum=Utils.hash(keynum);
        }*/
        String dbkey=operation._key;
        
        HashMap<String,String> values=new HashMap<String,String>();
     /*   for (int i=0; i<fieldcount; i++)
        {
            String fieldkey="field"+i;
            String data=Utils.ASCIIString(fieldlength);
            values.put(fieldkey,data);
        }*/
        
        for (String fieldname : operation._field)
        {
            String data=Utils.ASCIIString(fieldlength);         
            values.put(fieldname,data);
        }
        
        
        db.insert(table,dbkey,values);
    }
}
