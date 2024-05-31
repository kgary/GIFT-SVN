/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * A client-safe representation of a file directory on the server.
 *
 * @author nroberts
 */
public class FileTreeModel implements Serializable{
	
    private static final long serialVersionUID = 1L;

    /** The name of the file or directory this FileTreeModel represents. */
	private String fileOrDirectoryName;
	
	/**  
	 * The FileTreeModels of all the files and directories inside the directory represented by this FileTreeModel, if applicable. 
	 * Will be null if this is a file.  An empty list means that the folder has not descendant files or folders.
	 */
	private List<FileTreeModel> subFilesAndDirectories;
	
	/** The parent tree model. */
	private FileTreeModel parentTreeModel = null;
	
	/**
	 * Default public constructor required for serialization.
	 */
	public FileTreeModel(){
		
	}
	
	/**
	 * Instantiates a new file tree model representing a single file.
	 *
	 * @param fileName the file name
	 */
	public FileTreeModel(String fileName){
		this.setFileOrDirectoryName(fileName);
		this.setSubFilesAndDirectories(null);
	}
	
	/**
	 * Instantiates a new file tree model representing a directory.
	 *
	 * @param directoryName the directory name
	 * @param subFilesAndDirectories the subfiles and subdirectories inside the directory represented by this FileTreeModel
	 */
	public FileTreeModel(String directoryName, List<FileTreeModel> subFilesAndDirectories){
		this.setFileOrDirectoryName(directoryName);
		this.setSubFilesAndDirectories(subFilesAndDirectories);
	}
	
	/**
	 * Return whether or not this model has a child (file or folder) with the given name.  This will
	 * not search recursively through all descendants.
	 * 
	 * @param name the name of the child to find.  Can't be null or empty.
	 * @return true iff a child with the name was found.
	 */
	public boolean hasChildFileOrDirectoryName(String name){
	    
	    if(name == null || name.length() == 0){
	        throw new IllegalArgumentException("The name can't be null or empty.");
	    }else if(subFilesAndDirectories == null){
	        return false;
	    }
	    
	    for(FileTreeModel child : subFilesAndDirectories){
	        
	        if(child.getFileOrDirectoryName().equals(name)){
	            return true;
	        }
	    }
	    
	    return false;
	}
	
	/**
	 * Return the child model (file or folder) that has the given name (case-insensitive).  This will not
	 * search recursively through all descendants.
	 * 
	 * @param name the name of the child to find.  Can't be null or empty.
	 * @return the model for the child, if a child with the name was found. Null otherwise.
	 */
	public FileTreeModel getChildByName(String name){
	    
        if(name == null || name.length() == 0){
            throw new IllegalArgumentException("The name can't be null or empty.");
        }else if(subFilesAndDirectories == null){
            return null;
        }
       
        for(FileTreeModel child : subFilesAndDirectories){
            
            if(child.getFileOrDirectoryName().equalsIgnoreCase(name)){
                return child;
            }
        }
        
        return null;
	}

	/**
	 * Gets the file name if this FileTreeModel represents a file or gets the directory name if this FileTreeModel represents a directory.
	 *
	 * @return the file name
	 */
	public String getFileOrDirectoryName() {
		return fileOrDirectoryName;
	}

	/**
	 * Sets the file name if this FileTreeModel represents a file or Sets the directory name if this FileTreeModel represents a directory.
	 *
	 * @param fileName the new file name.  Can't be null or empty.
	 */
	public void setFileOrDirectoryName(String fileName) {
	    
        if(fileName == null || fileName.length() == 0){
            throw new IllegalArgumentException("The name can't be null or empty.");
        }
        
		this.fileOrDirectoryName = fileName;
	}

	/**
	 * If this FileTreeModel represents a directory, gets an unmodifiable list of the FileTreeModels of all the files and directories 
	 * inside that directory. Otherwise, returns null.
	 *
	 * @return if a directory, the FileTreeModels of all the files and directories inside. Otherwise, null.
	 */
	public List<FileTreeModel> getSubFilesAndDirectories() {	
		return Collections.unmodifiableList(subFilesAndDirectories);
	}
	
	/**
	 * Sets the FileTreeModels of all the files and directories inside the directory represented by this FileTreeModel and sets this 
	 * FileTreeModel as their parent.
	 *
	 * @param subFilesAndDirectories the subfiles and subdirectories inside the directory represented by this FileTreeModel
	 */
	public void setSubFilesAndDirectories(List<FileTreeModel> subFilesAndDirectories) {
		
		if(subFilesAndDirectories != null){
		
			for(FileTreeModel childModel : subFilesAndDirectories){
				childModel.setParentTreeModel(this);
			}
		}
		
		this.subFilesAndDirectories = subFilesAndDirectories;
	}
	
	/**
	 * Checks if this FileTreeModel represents a directory.
	 *
	 * @return true, if this FileTreeModel represents a directory
	 */
	public boolean isDirectory() {		
		return subFilesAndDirectories != null;
	}	
	
	/**
	 * Sets the parent tree model.
	 *
	 * @param parentTreeModel the new parent tree model
	 */
	private void setParentTreeModel(FileTreeModel parentTreeModel){
		this.parentTreeModel = parentTreeModel;
	}
	
	/**
	 * Gets the parent tree model. 
	 *
	 * @return the parent tree model. Can be null.
	 */
	public FileTreeModel getParentTreeModel(){
		return parentTreeModel;
	}
	
	/**
	 * Adds the sub file or directory.
	 *
	 * @param subFileOrDirectory the sub file or directory.  Can't be null.
	 * @return true, if successful
	 */
	public boolean addSubFileOrDirectory(FileTreeModel subFileOrDirectory){
	    
	    if(subFileOrDirectory == null){
	        throw new IllegalArgumentException("The subFileOrDirectory to add can't be null.");
	    }
		
		if(subFilesAndDirectories == null){
			subFilesAndDirectories = new ArrayList<FileTreeModel>();
		}
		
		subFileOrDirectory.setParentTreeModel(this);
		
		return subFilesAndDirectories.add(subFileOrDirectory);
	}
	
	/**
	 * Removes the sub file or directory.
	 *
	 * @param subFileOrDirectory the sub file or directory
	 * @return true, if successful
	 */
	public boolean removeSubFileOrDirectory(FileTreeModel subFileOrDirectory){
		
		if(subFilesAndDirectories != null){
			
			if(subFileOrDirectory.getParentTreeModel().equals(this)){
				subFileOrDirectory.setParentTreeModel(null);
			}
			
			return subFilesAndDirectories.remove(subFileOrDirectory);
		}
		
		return false;
	}
	
	/**
	 * Gets the relative path of this file tree up to its highest parent (i.e. the root).
	 * E.g. FileTreeModel of:
	 * A/
	 *   B/
	 *     one.txt
	 * 
	 * will return:
	 * i. "A/B/one.txt" on one.txt tree model
	 * ii. "A/B" on B tree model
	 * iii. A on A tree model
	 * 
	 * @return the relative path of this file tree up to its highest parent.  Will not be null or empty string.
	 */
	public String getRelativePathFromRoot(){
	    return getRelativePathFromRoot(false);
	}
	
	/**
     * Gets the relative path of this file tree up to its highest parent (i.e. the root).</br>
     * E.g. FileTreeModel of:</br>
     * A/</br>
     *   B/</br>
     *     one.txt</br>
     * </br>
     * for excludeRootDirectory = false, will return:</br>
     * i. "A/B/one.txt" for one.txt tree model</br>
     * ii. "A/B" for B tree model</br>
     * iii. A for A tree model</br>
     * </br>
     * for excludeRootDirectory = true, will return:</br>
     * i. "B/one.txt" for one.txt tree model</br>
     * ii. "B" for B tree model</br>
     * iii. "" for A tree model</br>
     * 
     * @param excludeRootDirectory whether or not to exclude the root directory from the returned path
     * @return the relative path of this file tree up to its highest parent.  Will not be null but could be empty string.
     */
    public String getRelativePathFromRoot(boolean excludeRootDirectory){
        
        if(parentTreeModel == null){
            
            if(isDirectory() && excludeRootDirectory){
                return Constants.EMPTY;
            }else{
                return fileOrDirectoryName; 
            }
        
        } else {
            
            String parentPath = parentTreeModel.getRelativePathFromRoot(excludeRootDirectory);
            if(parentPath.isEmpty()){
                return fileOrDirectoryName;
            }else{
                return parentPath + Constants.FORWARD_SLASH + fileOrDirectoryName;
            }
        }   
    }
	
	/**
	 * Gets a list of all descendant file names under this file tree model. 
	 * 
	 * @return a list of all descendant file names under this file tree model. 
	 */
	public List<String> getFileNamesUnderModel(){
		
		List<String> fileNamesUnderThisModel = new ArrayList<String>();
		
		if(this.isDirectory()){
			
			for(FileTreeModel child: subFilesAndDirectories){
				
				if(child.isDirectory()){
					
					List<String> fileNamesUnderChild = child.getFileNamesUnderModel();
					
					for(String fileName : fileNamesUnderChild){
						
						fileName = child.getFileOrDirectoryName() + Constants.FORWARD_SLASH + fileName;
						
						fileNamesUnderThisModel.add(fileName);
					}
				
				} else {
					
					fileNamesUnderThisModel.add(child.getFileOrDirectoryName());
				}
			}
			
		} 
		
		return fileNamesUnderThisModel;
	}
	
	/**
	 * The getAllFileTreeModels() function will return a list starting with the filetreemodel, plus all all descendants (both files & directories). If
	 * the filetreemodel is a file, then only the file itself is returned in the list.  If the filetreemodel is a directory,
	 * then the directory is added to the list, plus all descendants (which includes all sub folders/files) from the 
	 * directory.  This returns a list of all filetreemodel objects for all descendants.
	 * 
	 * @param modelList - a list (cannot be null) that will be returned with all descendant filetreemodels.  When calling this, pass in an empty array to start. 
	 */
	public void getAllFileTreeModels(List<FileTreeModel> modelList) {

	    if (modelList != null) {
	        
	        // Add the filetreemodel itself (whether it's a file or a directory).
	        modelList.add(this);
	        
	        // If the filetree model is a directory, loop through each child and build the list of all descendants.
	        if (this.isDirectory()) {
                for(FileTreeModel child: subFilesAndDirectories){
	                
	                if(child.isDirectory()){
	                    
	                    // Get all descendants.
	                    child.getAllFileTreeModels(modelList);
	                } else {
	                    
	                    modelList.add(child);
	                }
	            }
	        }
	        
	    }
	}
	
	/**
	 * Gets the FileTreeModel corresponding to the specified relative path of a file (not a directory) using this FileTreeModel as the root.  The 
	 * path provided should be included in this file tree model as some descendant.</br>
	 * Note: if the file doesn't exist a file tree model will be created for it.</br>
	 * Example 1:</br>
	 * FileTreeModel looks like - a, a/b, a/b/c.txt</br>
	 * relative path input = c.txt</br>
	 * returned FileTreeModel = a/c.txt  --- Note how this is wrong as c.txt is under a folder.</br>
	 * Example 2:</br>
     * FileTreeModel looks like - a, a/b, a/b/c.txt</br>
     * relative path input = b/c.txt</br>
     * returned FileTreeModel = a/b/c.txt</br>
	 * 
	 * 
	 * @param relativePath the relative path of the FileTreeModel to get.   Can't be null.
	 * @return the FileTreeModel to get. Null will be returned if this model is not relative to the
	 * path provided.  This will be a file not a directory file tree model representation.
	 */
	public FileTreeModel getModelFromRelativePath(String relativePath){
		
		return getModelFromRelativePath(relativePath, true, false);
	}
	
	/**
     * Gets the FileTreeModel corresponding to the specified relative path using this FileTreeModel as the root.  The 
     * path provided should be included in this file tree model as some descendant.</br>
     * Example 1:</br>
     * FileTreeModel looks like - a, a/b, a/b/c.txt</br>
     * relative path input = c.txt</br>
     * returned FileTreeModel = a/c.txt  --- Note how this is wrong as c.txt is under a folder.</br>
     * Example 2:</br>
     * FileTreeModel looks like - a, a/b, a/b/c.txt</br>
     * relative path input = b/c.txt</br>
     * returned FileTreeModel = a/b/c.txt</br>
	 * 
	 * @param relativePath the relative path of the FileTreeModel to get.   Can't be null.
	 * @param createIfNoneExists whether or not to create a corresponding FileTreeModel if none exists
	 * @return the FileTreeModel to get. Null will be returned if this model is not relative to the
	 * path provided.
	 */
	public FileTreeModel getModelFromRelativePath(String relativePath, boolean createIfNoneExists){
		
		return getModelFromRelativePath(relativePath, createIfNoneExists, false);
	}
	
	/**
     * Gets the FileTreeModel corresponding to the specified relative path using this FileTreeModel as the root.  The 
     * path provided should be included in this file tree model as some descendant.</br>
     * Example 1:</br>
     * FileTreeModel looks like - a, a/b, a/b/c.txt</br>
     * relative path input = c.txt</br>
     * returned FileTreeModel = a/c.txt  --- Note how this is wrong as c.txt is under a folder.</br>
     * Example 2:</br>
     * FileTreeModel looks like - a, a/b, a/b/c.txt</br>
     * relative path input = b/c.txt</br>
     * returned FileTreeModel = a/b/c.txt</br>
	 * 
	 * @param relativePath the relative path of the FileTreeModel to get.  Can't be null.
	 * @param createIfNoneExists whether or not to create a corresponding FileTreeModel if none exists
	 * @param returnAsDirectory whether or not the created FileTreeModel should be a directory
	 * @return the FileTreeModel to get. Null will be returned if this model is not relative to the
	 * path provided.
	 */
	public FileTreeModel getModelFromRelativePath(String relativePath, boolean createIfNoneExists, boolean returnAsDirectory){
		
		if(relativePath == null){
			throw new IllegalArgumentException("The path cannot be null.");
		}
		
		//replace backslashes with forward slashes
		if(relativePath.indexOf("\\") != -1){
			relativePath = relativePath.replace("\\", Constants.FORWARD_SLASH);
		}
		
		if(relativePath.startsWith(Constants.FORWARD_SLASH)){
			relativePath = relativePath.replaceFirst(Constants.FORWARD_SLASH, Constants.EMPTY);
		}
		
		if(relativePath.isEmpty()){
			return this;
		}
		
		String fileName = null;
		
		if(relativePath.indexOf(Constants.FORWARD_SLASH) != -1){
		
			fileName = relativePath.substring(0, relativePath.indexOf(Constants.FORWARD_SLASH));
		
		} else {
			
			fileName = relativePath;
		}
		
		//look for existing file tree model
		if(subFilesAndDirectories != null){
			for(FileTreeModel child : subFilesAndDirectories){
				
				if(child.getFileOrDirectoryName().equals(fileName)){
					
					if(child.isDirectory()){
						int beginIndex = relativePath.indexOf(fileName) + fileName.length();
						String newRelativepath = relativePath.substring(beginIndex);
						return child.getModelFromRelativePath(newRelativepath, createIfNoneExists, returnAsDirectory);
					
					} else {
						
						return child;
					}
				}
			}
		}
		
		if(createIfNoneExists){
		
			//if no existing file tree model is found matching the path, make one using the remainder of the path
			
			FileTreeModel newFileTreeModel = returnAsDirectory ? new FileTreeModel(fileName, new ArrayList<FileTreeModel>()) : new FileTreeModel(fileName);
			this.addSubFileOrDirectory(newFileTreeModel);  //this correctly creates the linkage both in the 'newFileTreeModel' and this model
			
			/* 
			 * Nick: We can't use String.replaceFirst(...) here since it doesn't escape regular expression characters and can't use 
			 * Pattern.quote(...) because Pattern isn't emulated by GWT; therefore, we need to get the remainder of the path manually 
			 * using indexes. 
			 * 
			 * Fortunately, since fileName is a substring of relativePath (or relativePath itself), we know we won't get out-of-bounds indexes.
			 */		
			String newRelativePath = relativePath.substring(relativePath.indexOf(fileName) + fileName.length(), relativePath.length());
			
			if(relativePath.equals(newRelativePath)){
			    //if the string didn't change then calling getModelFromRelativePath will cause a stack overflow
			    return null;
			}else{
			    return newFileTreeModel.getModelFromRelativePath(newRelativePath, createIfNoneExists, returnAsDirectory);
			}
			
		} else {
			return null;
		}
	}
	
	   public void sortSubFilesandDirectories(Comparator<FileTreeModel> comparator){
	        
	        if(isDirectory()){
	            
	            for(FileTreeModel file : subFilesAndDirectories){
	                file.sortSubFilesandDirectories(comparator);
	            }
	            
	            Collections.sort(subFilesAndDirectories, comparator);
	        }
	    }
	    
	    /**
	     * Removes this file tree model from its parent. This can be useful in cases where only part of a file hierarchy needs to be known.
	     * 
	     * @return the parent this file tree model was removed from
	     */
	    public FileTreeModel detatchFromParent(){
	        
	        FileTreeModel parent = this.getParentTreeModel();
	        if(parent != null && parent.subFilesAndDirectories != null) {
	            parent.subFilesAndDirectories.remove(this);
	        }
	        
	        this.parentTreeModel = null;
	        
	        return parent;
	    }
	    
	    /**
	     * Returns the root level {@link FileTreeModel FileTreeModel} for this object.
	     * If the FileTreeModel already is a 'root' level object, then the object itself is returned.
	     * 
	     * @return {@link FileTreeModel FileTreeModel} - The root level FileTreeModel.
	     */
	    public FileTreeModel getRootFileTreeModel() {
	       FileTreeModel foundModel;
	       if (parentTreeModel != null) {
	           foundModel = parentTreeModel.getRootFileTreeModel();
	       } else {
	           foundModel = this;
	       }
	       return foundModel;
	    }
	    
	    /**
	     * Creates a tree model based on the provided path. Note that this file tree model is not derived from the file system but generated
	     * from the path string itself, so the returned file tree model should NOT be used to gather state information (i.e. {@link #isDirectory()}, 
	     * {@link #isModifiable()}, etc.) or to navigate the file tree (i.e. {@link #getSubFilesAndDirectories()}). Basically, this file 
	     * tree model is analogous to a java.nio.file.Path object and is intended to simplify the process of dealing with paths in the GIFT
	     * file system rather than represent the file system.
	     * 
	     * @param path the path from which to generate the file tree model
	     * @return a file tree model representing the provided path
	     */
	    public static FileTreeModel createFromRawPath(String path){
	        
	        if(path != null){
	        
	            String correctedPath = correctFilePath(path);
	            
	            if(correctedPath.indexOf(Constants.FORWARD_SLASH) < 0){
	                
	                //no file separators, so only one file
	                return new FileTreeModel(correctedPath);
	                
	            } else {
	                
	                //one or more file separators, so at least one folder
	                String rootFileName = correctedPath.substring(0, correctedPath.indexOf(Constants.FORWARD_SLASH));
	                String remainder = correctedPath.substring(rootFileName.length());
	                
	                return new FileTreeModel(rootFileName).getModelFromRelativePath(remainder);
	            }
	            
	        } else {
	            return null;
	        }
	    }

	    /**
	     * Fixes minor problems with the given file path that can cause problems accessing its file in the file system, such as 
	     * including backslashes or starting the path with a redundant slash.
	     * 
	     * @param path the path to correct
	     * @return the corrected path
	     */
	    public static String correctFilePath(String path){
	        
	        String validPath = path;
	        
	        if(validPath != null){
	            
	            if(validPath.indexOf("\\") != -1){
	                validPath = validPath.replace(Constants.BACKWARD_SLASH, Constants.FORWARD_SLASH);
	            }
	            
	            if(validPath.startsWith(Constants.FORWARD_SLASH)){
	                validPath = validPath.substring(1);
	            }
	        }
	        
	        return validPath;
	    }
	
	@Override
	public String toString(){
	    
	    StringBuffer sb = new StringBuffer();
	    sb.append("[FileTreeModel: ");
	    sb.append("name = ").append(getFileOrDirectoryName());
	    
	    if(isDirectory()){
	        sb.append(", # of children = ").append(getSubFilesAndDirectories().size());
	    }else{
	        sb.append(", isFile = true");
	    }
	    
	    sb.append("]");
	    return sb.toString();
	}
	
}
