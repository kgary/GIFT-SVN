package mil.arl.gift.lms.impl.lrs.xapi.append;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.rusticisoftware.tincan.Context;
import mil.arl.gift.common.course.AssessmentChainOfCustody;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.Version;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.lms.impl.common.LmsXapiAppenderException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsContextExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;

/**
 * Appends the Chain of Custody context extension to xAPI Statement.
 * 
 * @author Yet Analytics
 *
 */
public class ChainOfCustodyAppender extends AbstractStatementAppender {
    /** extension to be populated and appended to statement */
    private ItsContextExtensionConcepts.ChainOfCustody extConcept;
    /** used within creation of domain sessions directory path */
    private Integer domainSessionId;
    /** used within creation of domain sessions directory path */
    private Integer userId;
    /** Information used to create extension */
    private AssessmentChainOfCustody chain;
    /** File Extension for DKF */
    private static final String DKF = ".dkf.xml";
    /** File Extension for protobuf domain session log files */
    private static final String PROTOBUF = ".protobuf.bin";
    /** File Extension for legacy domain session log files */
    private static final String LEGACY = ".log";
    /** name of the appender */
    private static final String appenderName = "Chain of Custody Appender";
    /** appender description */
    private static final String appenderInfo = "attaches info about instance of GIFT in which an xAPI Statement was generated";
    
    /**
     * Initialize Chain of Custody context extension.
     * 
     * @throws LmsXapiProfileException when unable to resolve extension from xAPI Profile
     */
    private ChainOfCustodyAppender() throws LmsXapiProfileException {
        super(appenderName, appenderInfo);
        this.extConcept = ItsContextExtensionConcepts.ChainOfCustody.getInstance();
    }
    /**
     * Sets domain session id and user id, both are required.
     * 
     * @param dsId - domain session id
     * @param uId - user id
     * 
     * @throws LmsXapiProfileException when unable to resolve extension from xAPI Profile
     */
    public ChainOfCustodyAppender(Integer dsId, Integer uId) throws LmsXapiProfileException {
        this();
        if(dsId == null) {
            throw new IllegalArgumentException("domain session id can not be null!"); 
        }
        this.domainSessionId = dsId;
        if(uId == null) {
            throw new IllegalArgumentException("user id can not be null!");
        }
        this.userId = uId;
    }
    /**
     * Sets source of extension values
     * 
     * @param chainInfo - source of extension values
     * 
     * @throws LmsXapiProfileException when unable to resolve extension from xAPI Profile
     */
    public ChainOfCustodyAppender(AssessmentChainOfCustody chainInfo) throws LmsXapiProfileException {
        this();
        if(chainInfo == null) {
            throw new IllegalArgumentException("Chain of custody information can not be null!");
        }
        this.chain = chainInfo;
    }
    
    /**
     * @return file system path to domain sessions directory
     */
    public String createDomainSessionsPath() {
        String buildPath = Version.getInstance().getBuildLocation();
        String directoryPath = buildPath+File.separator+"GIFT"+File.separator+"output"+File.separator+"domainSessions"+File.separator;
        return directoryPath+"domainSession"+domainSessionId+"_uId"+userId;
    }
    /**
     * parses domain session log files and dkf files from path.
     * 
     * @param path - file system path to domain session directory
     * 
     * @return collection of dkf + domain session log files found in directory
     * 
     * @throws LmsXapiAppenderException when unable to find domain session directory
     */
    public List<FileProxy> locateFiles(String path) throws LmsXapiAppenderException {
        File file = new File(path);
        List<FileProxy> found = new ArrayList<FileProxy>();
        if(file.exists()) {
            DesktopFolderProxy proxy = new DesktopFolderProxy(file);
            String[] extensions = {DKF, PROTOBUF, LEGACY};
            try {
                found = proxy.listFiles(new ArrayList<String>(0), extensions);
            } catch (IOException e) {
                throw new LmsXapiAppenderException("Unable to find files within domain session output folder!", e);
            }
        }
        return found;
    }
    /**
     * Splits files based on extension.
     * 
     * @param files - collection of files to split, not modified
     * @param dkfFileNames - accumulator for dkf files, modified
     * @param logFileNames - accumulator for domain session log files, modified
     */
    public void populateFileColls(List<FileProxy> files, Set<String> dkfFileNames, Set<String> logFileNames) {
        if(CollectionUtils.isNotEmpty(files)) {
            for(FileProxy file : files) {
                String fileName = file.getName();
                if(fileName.contains(DKF)) {
                    dkfFileNames.add(fileName);
                } else if(fileName.contains(PROTOBUF) || fileName.contains(LEGACY)) {
                    logFileNames.add(fileName);
                }
            }
        }
    }
    
    @Override
    public AbstractGiftStatement appendToStatement(AbstractGiftStatement statement) throws LmsXapiAppenderException {
        Context ctx = statement.getContext();
        String domainSessionPath;
        Set<String> dkfFileNames = new HashSet<String>();
        Set<String> logFileNames = new HashSet<String>();
        if(chain == null) {
            // legacy, TODO: replace once chain info in started domain session message
            domainSessionPath = createDomainSessionsPath();
            List<FileProxy> found = locateFiles(domainSessionPath);
            populateFileColls(found, dkfFileNames, logFileNames);
        } else {
            domainSessionPath = chain.getSessionoutputfolder();
            dkfFileNames.add(chain.getDkffilename());
            logFileNames.add(chain.getSessionlogfilename());
        }
        extConcept.addToContext(ctx, domainSessionPath, dkfFileNames, logFileNames);
        statement.setContext(ctx);
        return statement;
    }
}
