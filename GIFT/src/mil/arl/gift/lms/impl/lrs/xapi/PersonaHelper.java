package mil.arl.gift.lms.impl.lrs.xapi;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.rusticisoftware.tincan.Agent;
import com.rusticisoftware.tincan.AgentAccount;
import com.rusticisoftware.tincan.Group;
import com.rusticisoftware.tincan.internal.StatementBase;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.lms.impl.common.LmsXapiAgentException;
import mil.arl.gift.lms.impl.lrs.CommonLrsEnum;

/**
 * Utility class for handling xAPI Agents and Groups.
 * 
 * @author Yet Analytics
 *
 */
public class PersonaHelper {

    /**
     * Converts the user id to MBOX IFI.
     * 
     * @param userSlug - unique part of MBOX IFI
     * 
     * @return the MBOX IFI.
     * 
     * @throws LmsXapiAgentException when unable to create the MBOX IFI
     */
    public static String createMboxIFI(String userSlug) throws LmsXapiAgentException {
        if(userSlug == null || StringUtils.isBlank(userSlug)) {
            throw new LmsXapiAgentException("User Id cannot be null or empty when creating mbox IFI.");
        }
        String encoded;
        try {
            encoded = java.net.URLEncoder.encode(userSlug, CommonLrsEnum.ENCODING.getValue());
        } catch (UnsupportedEncodingException e) {
            throw new LmsXapiAgentException("Unable to encode userSlug: " + userSlug, e);
        }
        return CommonLrsEnum.MBOX_PREFIX.getValue() + encoded + CommonLrsEnum.EMAIL_DOMAIN.getValue();   
    }

    /**
     * Converts userName and homePage into AgentAccount
     * 
     * @param userName - string identifier used within account IFI - individual's account id
     * @param homePage - string identifier used within account IFI - canonical home page for a system the account is on
     * 
     * @return AgentAccount with homePage and name
     * 
     * @throws LmsXapiAgentException when unable to create account IFI
     */
    public static AgentAccount createAccountIFI(String userName, String homePage) throws LmsXapiAgentException {
        if(userName == null || StringUtils.isBlank(userName)) {
            throw new LmsXapiAgentException("User Name cannot be null or empty when creating account IFI.");
        }
        if(homePage == null || StringUtils.isBlank(homePage)) {
            throw new LmsXapiAgentException("Home Page cannot be null or empty when creating account IFI.");
        }
        AgentAccount account = new AgentAccount();
        account.setName(userName);
        account.setHomePage(homePage);
        return account;
    }

    /**
     * Creates an xAPI Agent with MBOX IFI from a user name 
     *
     * @param userSlug - string identifier used within xAPI Agent email and display name
     * 
     * @return the xAPI Agent with MBOX IFI and display name
     * 
     * @throws LmsXapiAgentException when unable to create the MBOX IFI
     */
    public static Agent createMboxAgent(String userSlug) throws LmsXapiAgentException {
        return createMboxAgent(userSlug, userSlug);
    }

    /**
     * Creates an xAPI Agent with MBOX IFI from userSlug and sets userName as name
     * 
     * @param userSlug - string identifier used within xAPI Agent email
     * @param userName - display name for the agent
     * 
     * @return the xAPI Agent with MBOX IFI and display name
     * 
     * @throws LmsXapiAgentException when unable to create the MBOX IFI
     */
    public static Agent createMboxAgent(String userSlug, String userName) throws LmsXapiAgentException {
        if(userName == null || StringUtils.isBlank(userName)) {
            throw new LmsXapiAgentException("Attempting to create mbox agent with null or empty name!");
        }
        Agent agent = new Agent();
        agent.setMbox(createMboxIFI(userSlug));
        agent.setName(userName);
        return agent;
    }

    /**
     * Creates an xAPI Agent with account IFI from userSlug and homePage, userSlug is used as display name
     * 
     * @param userSlug - string identifier used within account IFI - individual's account id
     * @param homePage - string identifier used within account IFI - canonical home page for a system the account is on
     * 
     * @return the xAPI Agent with account IFI and display name
     * 
     * @throws LmsXapiAgentException when unable to create the account IFI
     */
    public static Agent createAccountAgent(String userSlug, String homePage) throws LmsXapiAgentException {
        return createAccountAgent(userSlug, homePage, userSlug);
    }

    /**
     * Creates an xAPI Agent with account IFI from userSlug and homePage, displayName is used as display name
     * 
     * @param userSlug - string identifier used within account IFI - individual's account id
     * @param homePage - string identifier used within account IFI - canonical home page for a system the account is on
     * @param displayName - display name for the agent with the account IFI
     * 
     * @return the xAPI Agent with account IFI and display name
     * 
     * @throws LmsXapiAgentException when unable to create the account IFI
     */
    public static Agent createAccountAgent(String userSlug, String homePage, String displayName) throws LmsXapiAgentException {
        if(displayName == null || StringUtils.isBlank(displayName)) {
            throw new LmsXapiAgentException("Attempting to create Account agent with null or empty name!");
        }
        Agent agent = new Agent();
        agent.setAccount(createAccountIFI(userSlug, homePage));
        agent.setName(displayName);
        return agent;
    }

    /**
     * Create xAPI Agent representation for the GIFT platform
     * 
     * @return GIFT xAPI Agent
     * 
     * @throws LmsXapiAgentException when unable to create Agent
     */
    public static Agent createGiftAgent() throws LmsXapiAgentException {
        return createMboxAgent(CommonLrsEnum.GIFT.getValue());
    }
    
    /**
     * Creates an identified Group with MBOX IFI and display name corresponding to passed in name
     * 
     * @param name - name of the identified group
     * 
     * @return Group with MBOX IFI and display name
     * 
     * @throws LmsXapiAgentException when unable to create MBOX IFI from name
     */
    public static Group createGroup(String name) throws LmsXapiAgentException {
        String ifi = createMboxIFI(name);
        Group group = new Group();
        group.setMbox(ifi);
        group.setName(name);
        return group;
    }
    
    /**
     * Creates an anonymous Group with display name and collection of members
     * 
     * @param name - display name for the anonymous Group
     * @param member - Agents which belong to the Group
     * 
     * @return Group with display name and members
     */
    public static Group createGroup(String name, List<Agent> member) {
        Group group = createGroup(member);
        group.setName(name);
        return group;
    }
    
    /**
     * Creates an anonymous Group with members
     * 
     * @param member - Agents which belong to the Group
     * 
     * @return Group with members
     */
    public static Group createGroup(List<Agent> member) {
        Group group = new Group();
        group.setMembers(member);
        return group;
    }
    
    /**
     * Creates Agent or Group from collection of user names.
     * 
     * @param memberNames - collection of user names, can not be null
     * 
     * @return Agent or Group based on number of names within memberNames
     * 
     * @throws LmsXapiAgentException when unable to create an Agent from a user name
     */
    public static Agent createActor(List<String> memberNames) throws LmsXapiAgentException {
        int memNameSize = memberNames.size();
        if(memberNames == null || memNameSize == 0) {
            throw new IllegalArgumentException("Must provide a least one member name!");
        } else if(memNameSize == 1) {
            return createMboxAgent(memberNames.get(0));
        } else {
            List<Agent> members = new ArrayList<Agent>(memberNames.size());
            for(String name : memberNames) {
                members.add(createMboxAgent(name));
            }
            return createGroup(members);
        }
    }

    /**
     * Parses display name from Agent
     * 
     * @param agent - Agent to parse name from
     * 
     * @return non null display name
     * 
     * @throws LmsXapiAgentException when display name is null or blank
     */
    public static String parseAgentName(Agent agent) throws LmsXapiAgentException {
        String s = agent.getName();
        if(s == null || StringUtils.isBlank(s)) {
            throw new LmsXapiAgentException("Unable to parse name from unnamed agent!");
        }
        return s;
    }
    
    /**
     * Creates comma separated composition of all group member display names
     * 
     * @param group - Group with non null, non empty members
     * 
     * @return comma separated string of all member display names
     * 
     * @throws LmsXapiAgentException when Group doesn't contain members or has empty members
     */
    public static String composeMemberNames(Group group) throws LmsXapiAgentException {
        List<Agent> members = group.getMembers();
        if(members == null || CollectionUtils.isEmpty(members)) {
            throw new LmsXapiAgentException("Unable to parse names from memberless group!");
        }
        Set<String> names = new HashSet<String>();
        for(Agent a : members) {
            names.add(parseAgentName(a));
        }
        List<String> memberNames = new ArrayList<String>(members.size());
        memberNames.addAll(names);
        return StringUtils.join(CommonLrsEnum.SEPERATOR_COMMA.getValue(), memberNames);
    }

    /**
     * Parses display name from group if set, otherwise parses all group member display names and
     * as single comma separated string
     * 
     * @param group - Group to parse
     * 
     * @return Group display name or composition of member display names
     * 
     * @throws LmsXapiAgentException when Group doesn't have display name or members
     */
    public static String parseGroupName(Group group) throws LmsXapiAgentException {
        try {
            return parseAgentName(group);
        } catch (@SuppressWarnings("unused") LmsXapiAgentException e) {
            return composeMemberNames(group);
        }
    }

    /**
     * Parses display name from Statement Actor, when the Actor is a Group, the Group's display name or composition of member display names
     * 
     * @param stmt - Statement or SubStatement to parse Actor from
     * 
     * @return display name of Agent / Group Actor or composition of Group member display names
     * 
     * @throws LmsXapiAgentException when unable to parse Actor, when Agent display name is missing / blank, when Group display name is missing / blank or when there are no group members  
     */
    public static String getActorName(StatementBase stmt) throws LmsXapiAgentException {
        if(stmt.getActor() == null) {
            throw new LmsXapiAgentException("Unable to parse Actor name from null!");
        }
        Agent actor = stmt.getActor();
        String actorType = actor.getObjectType();
        if(actorType.equals(CommonLrsEnum.AGENT.getValue())) {
            return parseAgentName(actor);
        } else if(actorType.equals(CommonLrsEnum.GROUP.getValue())) {
            return parseGroupName((Group) actor);
        } else {
            throw new LmsXapiAgentException("Statement Actor was of unexpected objectType!");
        }
    }
}
