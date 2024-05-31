/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db.table;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@Table(name="datafile")
public class DbDataFile{

	private int fileId;
	private String fileName;	
	
	/**
	 * Default constructor - required by Hibernate
	 */
	public DbDataFile(){
		
	}

	/**
	 * Class Constructor 
	 * 
	 * @param fileName - the data file name
	 */
	public DbDataFile(String fileName) {
		this.fileName = fileName;
	}
	
	@Id
	@Column(name="fileId_PK")
	@TableGenerator(name="fileid", table="filepktb", pkColumnName="filekey", pkColumnValue="filevalue", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.TABLE, generator="fileid")
	public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}

	@Column(nullable=false)
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	@Override
	public String toString(){
		
	    StringBuffer sb = new StringBuffer();
	    sb.append(" [DataFile:");
	    sb.append(" file id = ").append(getFileId());
	    sb.append(" fileName = ").append(getFileName());
	    sb.append("]");
		
		return sb.toString();
	}	
}
