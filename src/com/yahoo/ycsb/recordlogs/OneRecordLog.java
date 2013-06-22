package com.yahoo.ycsb.recordlogs;

import java.io.IOException;

import com.yahoo.ycsb.measurements.exporter.MeasurementsExporter;

public abstract class OneRecordLog
{
    String _name;
    
    public String getName() {
        return _name;
    }

    /**
     * @param _name
     */
    public OneRecordLog(String _name) {
        this._name = _name;
    }

    //public abstract void reportReturnCode(int code);

    public abstract void recordlog(String content);

    //public abstract String getSummary();

  /**
   * Export the current measurements to a suitable format.
   * 
   * @param exporter Exporter representing the type of format to write to.
   * @throws IOException Thrown if the export failed.
   */
  public abstract void exportRecordLogs(MeasurementsExporter exporter) throws IOException;
}
