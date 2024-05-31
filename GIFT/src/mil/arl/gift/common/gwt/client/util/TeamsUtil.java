/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.util;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import generated.dkf.AGL;
import generated.dkf.Coordinate;
import generated.dkf.GCC;
import generated.dkf.GDC;
import generated.dkf.LearnerId;
import generated.dkf.StartLocation;
import generated.dkf.Team;
import generated.dkf.TeamMember;
import mil.arl.gift.common.coordinate.AbstractCoordinate;
import mil.arl.gift.common.course.dkf.team.AbstractTeamUnit;
import mil.arl.gift.common.course.dkf.team.LocatedTeamMember;
import mil.arl.gift.common.course.dkf.team.MarkedTeamMember;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;

/**
 * A class used to perform common operations with teams.
 * 
 * @author sharrison
 */
public class TeamsUtil {

    /**
     * Gets the names of all the team members that can be found among the teams
     * and team members with the given names. Teams with any of the given names
     * will be deeply searched to see if any of their children are team members
     * or have any child team members.
     * 
     * @param teamNames the team and team member names to search
     * @param team the root team to start with
     * @param hasAncestorWithName whether the root team has any ancestors with
     *        one of the given names
     * @return the team members found among the teams and team members with the
     *         given names
     */
    public static List<String> getTeamMemberNames(List<String> teamNames, Team team, boolean hasAncestorWithName) {

        List<String> teamMemberNames = new ArrayList<String>();
        if (team == null || teamNames == null) {
            return teamMemberNames;
        }

        /* We do not want to modify the provided list of team names so create a
         * copy to perform the actions on */
        List<String> teamNamesCopy = new ArrayList<>(teamNames);

        boolean selfOrAncestorHasName = hasAncestorWithName;

        if (teamNamesCopy.contains(team.getName())) {
            selfOrAncestorHasName = true;
            teamNamesCopy.remove(team.getName());
        }

        for (Serializable unit : team.getTeamOrTeamMember()) {

            if (unit instanceof TeamMember) {
                String memberName = ((TeamMember) unit).getName();
                boolean hasName = teamNamesCopy.contains(((TeamMember) unit).getName());

                if (selfOrAncestorHasName || hasName) {
                    if (hasName) {
                        teamNamesCopy.remove(memberName);
                    }

                    /* if we find a team member that has one of the specified
                     * names OR has an ancestor with one of the specified names,
                     * add it to the result */
                    teamMemberNames.add(memberName);
                }
            } else if (unit instanceof Team) {
                teamMemberNames.addAll(getTeamMemberNames(teamNamesCopy, (Team) unit, selfOrAncestorHasName));
            }
        }

        return teamMemberNames;
    }

    /**
     * Searches through the given team and returns the first team member name
     * found with it.
     * 
     * @param team the team within which to search for a team member name
     * @return the first team member name found, or null if no team members are
     *         found
     */
    public static String getAnyTeamMemberName(Team team) {
        if (team == null) {
            return null;
        }

        for (Serializable unit : team.getTeamOrTeamMember()) {

            if (unit instanceof TeamMember) {
                String name = ((TeamMember) unit).getName();
                if (name != null) {
                    return name;
                }

            } else if (unit instanceof Team) {
                String teamMemberName = getAnyTeamMemberName((Team) unit);
                if (teamMemberName != null) {
                    return teamMemberName;
                }
            }
        }

        return null;
    }

    /**
     * Finds the entity in the team organization with the provided name.
     * 
     * @param team the team to search.
     * @param name the name of the entity to find.
     * @param includeTeams true to return a {@link Team team} with the given
     *        name if found; false to only search for {@link TeamMember team
     *        members}.
     * @return the team org entity with the provided name. Will be null if the
     *         name was not found.
     */
    public static Serializable getTeamOrgEntityWithName(String name, Team team, boolean includeTeams) {
        if (includeTeams && StringUtils.equalsIgnoreCase(team.getName(), name)) {
            return team;
        }

        for (Serializable teamOrMember : team.getTeamOrTeamMember()) {
            if (teamOrMember instanceof Team) {
                Serializable entity = getTeamOrgEntityWithName(name, (Team) teamOrMember, includeTeams);
                if (entity != null) {
                    return entity;
                }
            } else if (teamOrMember instanceof TeamMember) {
                TeamMember member = (TeamMember) teamOrMember;
                if (StringUtils.equalsIgnoreCase(member.getName(), name)) {
                    return member;
                }
            }
        }

        return null;
    }

    /**
     * Gets the team member with the given name in the given team
     * 
     * @param names the names of the team members to get
     * @param team the team within which to look for the team member
     * @return the team member with the given name, or null, if no such team
     *         member exists
     */
    public static List<TeamMember> getTeamMembersWithNames(List<String> names, Team team) {

        List<TeamMember> returnList = new ArrayList<TeamMember>();
        if (team == null || names == null || names.isEmpty()) {
            return returnList;
        }

        /* We do not want to modify the provided list of names so create a copy
         * to perform the actions on */
        List<String> namesCopy = new ArrayList<>(names);

        for (Serializable unit : team.getTeamOrTeamMember()) {
            if (unit instanceof TeamMember) {
                TeamMember member = (TeamMember) unit;

                Iterator<String> itr = namesCopy.iterator();
                while (itr.hasNext()) {

                    String name = itr.next();
                    if (StringUtils.equals(name, ((TeamMember) unit).getName())) {
                        /* found one of the names in a team member, so stop
                         * looking for it */
                        returnList.add(member);
                        itr.remove();
                        break;
                    }
                }

            } else if (unit instanceof Team) {
                List<TeamMember> result = getTeamMembersWithNames(namesCopy, (Team) unit);
                if (result != null) {
                    returnList.addAll(result);
                }
            }
        }

        return returnList;
    }

    /**
     * Searches the given team for teams and team members with the given names
     * and returns the top-most names that are found. If one of the provided
     * names is used by a parent team, then the names of that parent team's
     * children will be removed from the returned list so that only the highest
     * name in the organization is kept. If the names of all the team members
     * within a parent team are part of the provided list, then only the name of
     * the parent team will be returned. Provided names that are not used by any
     * teams and team members will also be removed from the returned list.
     * 
     * This method essentially returns the smallest possible list of team and
     * team member names in which the given list of team and team member names
     * can be found
     * 
     * @param names the team and team member names to look for
     * @param team the team within which to look for names
     * @return the names of the top-most teams and team members that were found
     */
    public static List<String> getTopMostTeamNames(List<String> names, Team team) {

        List<String> returnList = new ArrayList<>();
        if (team == null || names == null) {
            return returnList;
        }

        /* We do not want to modify the provided list of names so create a copy
         * to perform the actions on */
        List<String> namesCopy = new ArrayList<>(names);

        if (!namesCopy.isEmpty()) {
            Iterator<String> itr = namesCopy.iterator();
            while (itr.hasNext()) {

                String name = itr.next();
                if (StringUtils.equals(name, team.getName())) {

                    /* found one of the names in a team, so stop looking for it
                     * and skip the team's children */
                    returnList.add(name);
                    itr.remove();

                    return returnList;
                }
            }
        }

        /* track whether all of this team's team members match the names being
         * looked for */
        boolean allMembersFound = true;

        if (namesCopy.isEmpty()) {
            allMembersFound = false;
        } else {
            for (Serializable unit : team.getTeamOrTeamMember()) {

                if (unit instanceof TeamMember) {
                    boolean nameFound = false;

                    Iterator<String> itr = namesCopy.iterator();
                    while (itr.hasNext()) {

                        String name = itr.next();
                        if (StringUtils.equals(name, ((TeamMember) unit).getName())) {
                            /* found one of the names in a team member, so stop
                             * looking for it */
                            nameFound = true;
                            returnList.add(name);
                            itr.remove();
                            break;
                        }
                    }

                    if (!nameFound) {
                        /* this member did not have a name being looked for */
                        allMembersFound = false;
                    }

                } else if (unit instanceof Team) {

                    Team subTeam = (Team) unit;
                    List<String> namesInSubTeam = getTopMostTeamNames(namesCopy, subTeam);

                    if (namesInSubTeam.size() != 1 || !namesInSubTeam.get(0).equals(subTeam.getName())) {
                        /* at least one member this a sub-team did not have a
                         * name being looked for */
                        allMembersFound = false;
                    }

                    returnList.addAll(namesInSubTeam);
                }
            }

        }

        if (!team.getTeamOrTeamMember().isEmpty() && allMembersFound) {
            /* ALL of this team's members had names being looked for, so only
             * return this team's name as a shorthand for them */
            returnList.clear();
            returnList.add(team.getName());
        }

        return returnList;
    }

    /**
     * Returns all the team member names within the provided team (recursively).
     * 
     * @param team the team to search.
     * @return all the team's members including sub-team members. Will never be
     *         null.
     */
    public static Set<String> getTeamMembersFromTeam(Team team) {
        Set<String> members = new HashSet<>();

        for (Serializable teamOrMember : team.getTeamOrTeamMember()) {
            if (teamOrMember instanceof Team) {
                members.addAll(getTeamMembersFromTeam((Team) teamOrMember));
            } else if (teamOrMember instanceof TeamMember) {
                TeamMember member = (TeamMember) teamOrMember;
                members.add(member.getName());
            }
        }

        return members;
    }

    /**
     * Gets whether or not there are any team members that use entity markers to
     * identify themselves in the given team
     * 
     * @param team the team to find team members with entity markers inside
     * @return whether any team members use entity markers
     */
    public static boolean hasTeamMemberWithMarker(Team team) {
        if (team == null) {
            return false;
        }

        for (Serializable unit : team.getTeamOrTeamMember()) {

            if (unit instanceof TeamMember) {

                TeamMember member = (TeamMember) unit;

                if (member.getLearnerId() != null && member.getLearnerId().getType() instanceof String) {
                    return true;
                }

            } else if (unit instanceof Team) {

                boolean result = hasTeamMemberWithMarker((Team) unit);

                if (result) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Finds any team members from the provided set that do not exist anywhere
     * in the specified team.
     * 
     * @param teamMembers the team members to find in the team.
     * @param team the team to search. If null, all team members will be
     *        invalid.
     * @return the collection of team member names that do not exist in the
     *         team; will return an empty set if all members were found. Will
     *         never return null.
     */
    public static Set<String> findInvalidMembers(Collection<String> teamMembers, Team team) {
        Set<String> invalidMembers = new HashSet<>();
        if (CollectionUtils.isEmpty(teamMembers)) {
            return null;
        } else if (team == null || team.getTeamOrTeamMember().isEmpty()) {
            invalidMembers.addAll(teamMembers);
            return invalidMembers;
        }

        /* Check if any members do not exist */
        for (String member : teamMembers) {
            if (!TeamsUtil.hasTeamOrTeamMemberWithName(member, team)) {
                invalidMembers.add(member);
            }
        }

        return invalidMembers;
    }

    /**
     * Checks if the given name matches a team or team member in the given team.
     * 
     * @param name the name of the team or team member to find.
     * @param team the team within which to look for the name.
     * @return true if the name was found; false otherwise. Will return false if
     *         the team is null or if the name is blank.
     */
    public static boolean hasTeamOrTeamMemberWithName(String name, Team team) {
        if (team == null || StringUtils.isBlank(name)) {
            return false;
        } else if (StringUtils.equals(name, team.getName())) {
            return true;
        }

        for (Serializable unit : team.getTeamOrTeamMember()) {

            if (unit instanceof TeamMember) {
                TeamMember member = (TeamMember) unit;
                if (StringUtils.equals(name, member.getName())) {
                    return true;
                }
            } else if (unit instanceof Team) {
                boolean result = hasTeamOrTeamMemberWithName(name, (Team) unit);
                if (result) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Finds the parent {@link Team} of the provided {@link Team} or
     * {@link TeamMember}.
     * 
     * @param teamToSearch the {@link Team} to search for the provided
     *        {@link Team} or {@link TeamMember}. Can't be null.
     * @param teamOrTeamMemberToFind the {@link Team} or {@link TeamMember} to
     *        find in order to retrieve its parent.
     * @return the parent {@link Team} or null if no parent is found.
     */
    public static Team findTeamParent(Team teamToSearch, Serializable teamOrTeamMemberToFind) {
        if (teamToSearch == null) {
            throw new IllegalArgumentException("The parameter 'teamToSearch' cannot be null.");
        } else if (teamOrTeamMemberToFind == null) {
            throw new IllegalArgumentException("The parameter 'teamOrTeamMemberToFind' cannot be null.");
        } else if (!(teamOrTeamMemberToFind instanceof Team || teamOrTeamMemberToFind instanceof TeamMember)) {
            throw new IllegalArgumentException(
                    "The parameter 'teamOrTeamMemberToFind' must be of type 'Team' or 'TeamMember'.");
        }

        /* Check the children of the current node to determine if the current
         * node is the parent */
        final List<Serializable> teamOrTeamMemberList = teamToSearch.getTeamOrTeamMember();
        for (Serializable teamOrMember : teamOrTeamMemberList) {
            if (teamOrTeamMemberToFind.equals(teamOrMember)) {
                return teamToSearch;
            } else if (teamOrMember instanceof Team) {
                /* Search another layer down the tree */
                Team result = findTeamParent((Team) teamOrMember, teamOrTeamMemberToFind);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    public static Team convertToDkfTeam(mil.arl.gift.common.course.dkf.team.Team courseTeam) {
        if (courseTeam == null) {
            return null;
        }

        Team dkfTeam = new Team();
        dkfTeam.setName(courseTeam.getName());

        List<Serializable> dkfUnits = dkfTeam.getTeamOrTeamMember();
        for (AbstractTeamUnit courseUnit : courseTeam.getUnits()) {
            if (courseUnit instanceof mil.arl.gift.common.course.dkf.team.TeamMember) {
                mil.arl.gift.common.course.dkf.team.TeamMember<?> courseTeamMember = (mil.arl.gift.common.course.dkf.team.TeamMember<?>) courseUnit;

                TeamMember dkfTeamMember = new TeamMember();
                dkfTeamMember.setName(courseTeamMember.getName());
                dkfTeamMember.setPlayable(courseTeamMember.isPlayable());
                final LearnerId learnerId = new LearnerId();
                dkfTeamMember.setLearnerId(learnerId);

                if (courseTeamMember instanceof LocatedTeamMember) {
                    LocatedTeamMember ltm = (LocatedTeamMember) courseTeamMember;
                    StartLocation start = new StartLocation();
                    start.setCoordinate(convertToDkfCoordinate(ltm.getIdentifier()));
                    learnerId.setType(start);
                } else if (courseTeamMember instanceof MarkedTeamMember) {
                    MarkedTeamMember mtm = (MarkedTeamMember) courseTeamMember;
                    learnerId.setType(mtm.getIdentifier());
                }

                dkfUnits.add(dkfTeamMember);
            } else if (courseUnit instanceof mil.arl.gift.common.course.dkf.team.Team) {
                dkfUnits.add(convertToDkfTeam((mil.arl.gift.common.course.dkf.team.Team) courseUnit));
            }
        }

        return dkfTeam;
    }

    /**
     * Converts an AbstractCoordinate to  the equivalent generated.dkf.Coordinate.
     * 
     * @param abstractCoordinate The coordinate to be converted.
     * @return a generated.dkf.Coordinate with a type and values equivalent to the abstractCoordinate.
     * If the abstractCoordinate is null, or if it has a coordinate type other than GCC, GDC, or AGL, returns null.
     */
    public static Coordinate convertToDkfCoordinate(AbstractCoordinate abstractCoordinate) {

        if (abstractCoordinate == null) {
            return null;
        }

        Coordinate coordinate = new Coordinate();
        if (abstractCoordinate instanceof mil.arl.gift.common.coordinate.GCC) {
            mil.arl.gift.common.coordinate.GCC oldGCC = (mil.arl.gift.common.coordinate.GCC) abstractCoordinate;
            GCC newGCC = new GCC();
            newGCC.setX(BigDecimal.valueOf(oldGCC.getX()));
            newGCC.setY(BigDecimal.valueOf(oldGCC.getY()));
            newGCC.setZ(BigDecimal.valueOf(oldGCC.getZ()));
            coordinate.setType(newGCC);

        } else if (abstractCoordinate instanceof mil.arl.gift.common.coordinate.GDC) {
            mil.arl.gift.common.coordinate.GDC oldGDC = (mil.arl.gift.common.coordinate.GDC) abstractCoordinate;
            GDC newGDC = new GDC();
            newGDC.setLongitude(BigDecimal.valueOf(oldGDC.getLongitude()));
            newGDC.setLatitude(BigDecimal.valueOf(oldGDC.getLatitude()));
            newGDC.setElevation(BigDecimal.valueOf(oldGDC.getElevation()));
            coordinate.setType(newGDC);

        } else if (abstractCoordinate instanceof mil.arl.gift.common.coordinate.AGL) {
            mil.arl.gift.common.coordinate.AGL oldAGL = (mil.arl.gift.common.coordinate.AGL) abstractCoordinate;
            AGL newAGL = new AGL();
            newAGL.setX(BigDecimal.valueOf(oldAGL.getX()));
            newAGL.setY(BigDecimal.valueOf(oldAGL.getY()));
            newAGL.setElevation(BigDecimal.valueOf(oldAGL.getZ()));
            coordinate.setType(newAGL);
        } else {
            /* Unknown type */
            return null;
        }

        return coordinate;
    }
}