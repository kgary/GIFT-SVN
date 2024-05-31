package mil.arl.gift.domain.knowledge;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import mil.arl.gift.common.course.dkf.session.KnowledgeAssessmentDetails;
import mil.arl.gift.common.course.dkf.session.KnowledgeAssessmentDetails.KnowledgeSessionVariable;
import mil.arl.gift.domain.knowledge.conversation.ConversationVarsHandler;

/**
 * A helper class used to track semantic 'variables' within the GIFT assessment model that can be shared
 * with sources outside the Domain module for tracking or decision-making purposes.
 * 
 * The variables used by this class shouldn't be seen as a replacement for regular Java variables within
 * classes and should be used sparingly. That said, they can be useful for sharing data that for 
 * calculations that require multiple data sources (e.g. using a DetectObjectsCondition and an
 * EliminateHostilesCondition, how long does it take to eliminate the targets once they are detected).
 * 
 * @author nroberts
 */
public class VariablesHandler {
	
	/** A mapping that is used to access variables via their composite keys */
	private Map<AbstractVariableKey<?>, Object> variableToVal = new HashMap<>();
	
	/** A sub-handler used to handle variables specific to conversations */
	private ConversationVarsHandler conversationVars;
	
	/**
	 * Gets a JSON object model of ALL the variables that are currently stored
	 * for the knowledge session scenario that this handler is tied to
	 * 
	 * @param details an optional set of assessment details to share information 
	 * from the model with.
	 * @return the variables model. Will not be null.
	 */
	@SuppressWarnings("unchecked")
	public JSONObject getAssessmentVariablesModel(KnowledgeAssessmentDetails details) {
		
		JSONObject modelObj = new JSONObject();
		JSONArray varsObj = new JSONArray();
		modelObj.put("variables", varsObj);
		
		for(AbstractVariableKey<?> key : variableToVal.keySet()) {
		    Object value = variableToVal.get(key);
			varsObj.add(key.printToJSON(value));
			
			if(details != null) {
			    
			    /* Populate the details to track variables in the domain session log*/
			    String name = key.variableName.getName();
			    String valueStr = value != null ? value.toString() : null;
			    String units = key.variableName.getUnits().getName();
			    String actorStr = null;
			    
			    if(key instanceof ActorVariableKey) {
			        AbstractAssessmentActor<?> actor = ((ActorVariableKey<?,?>) key).actor;
			        if(actor instanceof TeamMemberActor) {
			            actorStr = actor.toString();
			        }
			    }
			    
			    details.addVariable(new KnowledgeSessionVariable(name, valueStr, units, actorStr));
			}
		}
		
		return modelObj;
	}
	
	/**
	 * Sets a sub-handler to handle conversation-specific variables
	 * 
	 * @param conversationVars the conversation variable handler. Cannot be null.
	 */
	public void setConversationVars(ConversationVarsHandler conversationVars) {
		this.conversationVars = conversationVars;
	}
	
	/**
	 * Gets the sub-handler that handles conversation-specific variables
	 * 
	 * @return the conversation variable handler. Cannot be null.
	 */
	public ConversationVarsHandler getConversationVars() {
		return conversationVars;
	}
	
	/**
	 * Sets an actor-specific variable to the given value
	 * 
	 * @param <T> the type of value that can be set. Enforced by the variable name.
	 * @param actor the actor to whom the given variable applies. Cannot be null.
	 * @param variableName the name of the actor variable being set. Cannot be null.
	 * @param value the value to set the variable to. Cannot be null.
	 */
	public <T> void setVariable(AbstractAssessmentActor<?> actor, ActorVariables<T> variableName, T value) {
		variableToVal.put(new ActorVariableKey<>(variableName, actor), value);
	}
	
	/**
	 * Gets the value of an actor-specific variable
	 * 
	 * @param <T> the type of value that can be returned. Enforced by the variable name.
	 * @param actor the actor to whom the given variable applies. Cannot be null.
	 * @param variableName the name of the actor variable being retrieved. Cannot be null.
	 * @return the valule of the variable. Can be null if the variable has not been set or was
	 * explicitly set to null.
	 */
	/* Leaving type safety unchecked here is perfectly safe since the variable name ensures
	 * type safety to all values that can be assigned.*/
	@SuppressWarnings("unchecked")
	public <T> T getVariable(AbstractAssessmentActor<?> actor, ActorVariables<T> variableName) {
		
		Object value = variableToVal.get(new ActorVariableKey<>(variableName, actor));
		if(value == null) {
			return null;
		}
		
		return (T) value;
	}
	
	/**
	 * The types of semantic 'units' that assessment variables can use to measure their values. These
	 * are mainly used to distinguish the how the value is intended to be interpreted, as opposed to
	 * it's raw Java type. For instance, given a Long value, is it a duration, a timestamp, a count, etc?
	 * 
	 * @author nroberts
	 *
	 * @param <T> the Java type associated with a given unit. Used to ensure type safety.
	 */
	private static class VariableUnits<T>{
		
		/** A duration expressed in a number of milliseconds */
		private static final VariableUnits<Long> DURATION_MILLIS = new VariableUnits<>("duration_ms");
		
		/** A timestamp expressed in a number of milliseconds */
		private static final VariableUnits<Long> TIMESTAMP_MILLIS = new VariableUnits<>("timestamp_ms");
		
		/** The unique name of the unit */
		private String name;

		/**
		 * Creates a new unit with the given unique name
		 * 
		 * @param name the name of the unit. Cannot be null and must be unique.
		 */
		public VariableUnits(String name) {
			if(name == null) {
				throw new IllegalArgumentException("The name of a variable unit cannot be null");
			}
			
			this.name = name;
		}
		
		/**
		 * Gets the name of the unit
		 * 
		 * @return the unit name. Cannot be null.
		 */
		public String getName() {
			return name;
		}
	}
	
	/**
	 * An interface describing the bare-minimum contract of what constitutes an assessment variable
	 * definition. This contract is formed as a 3-part tuple: the variable name, the type of value
	 * that the variable expects, and the units used to measure that value. The variable name itself
	 * enforces that implementers of this interface provide all 3 of the constituent parts.
	 * 
	 * @author nroberts
	 *
	 * @param <T> the type of value expected by the variable.
	 */
	private static interface VariableName <T>{
		
		/**
		 * Gets the name of this variable
		 * 
		 * @return the variable name. Cannot be null.
		 */
		public String getName();
		
		/**
		 * Gets the units used to measure this variable's values
		 * 
		 * @return the units. Cannot be null.
		 */
		public VariableUnits<T> getUnits();
	}
	
	/**
	 * An abstract representation of some entity within a training environment that performs a specific
	 * role in the calculation of automated assessments, i.e. an actor. A good example of this are 
	 * targets, which can be any arbitrary objects that a learner has been tasked with shooting.
	 * 
	 * @author nroberts
	 *
	 * @param <T> a specific type of assessment actor.
	 */
	public static abstract class AbstractAssessmentActor<T>{
		
		/** The object that uniquely identifies this actor */
		protected T identifier;
		
		/**
		 * Creates a new assessment actor with the given identifier
		 * 
		 * @param identifier an object that uniquely identifies this actor.
		 */
		protected AbstractAssessmentActor(T identifier) {
			this.identifier = identifier;
		}
		
		/**
		 * Gets the identifier that uniquely represents this actor
		 * 
		 * @return the identifier. Will not be null.
		 */
		public T getIdentifier() {
			return identifier;
		}

		@Override
		public int hashCode() {
			return Objects.hash(identifier);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AbstractAssessmentActor<?> other = (AbstractAssessmentActor<?>) obj;
			return Objects.equals(identifier, other.identifier);
		}
	}
	
	/**
	 * An assessment actor representing a member of the knowledge session's team organization
	 * 
	 * @author nroberts
	 */
	public static class TeamMemberActor extends AbstractAssessmentActor<String>{

		/**
		 * Creates a team member actor that represents the team member with the given name
		 * 
		 * @param roleName the name of the role of the team member in the team organization.
		 * Can be null, though generally this should be populated.
		 */
		public TeamMemberActor(String roleName) {
			super(roleName);
		}
		
		@Override
		public String toString() {
			return identifier;
		}
	}
	
	/**
	 * A description of a variable whose values are specific to a particular actor. These can be useful
	 * for information regarding the state of a given actor within a scenario, for example, how long
	 * did it take learners to shoot the actor.
	 * 
	 * @author nroberts
	 *
	 * @param <T> the type of value that this variable accepts.
	 */
	public static class ActorVariables<T> implements VariableName<T>{
		
		/** The timestamp of when a target is exposed */
		public static final ActorVariables<Long> TARGET_UP = new ActorVariables<>("Target Up", VariableUnits.TIMESTAMP_MILLIS);
		
		/** The amount of time it takes to detect a target (i.e. target enters FoV) */
		public static final ActorVariables<Long> TARGET_DETECTION_TIME = new ActorVariables<>("Target Detection Time", VariableUnits.DURATION_MILLIS);
		
		/** The amount of time it takes for learners to aim towards a target */
		public static final ActorVariables<Long> TARGET_ORIENTATION_TIME = new ActorVariables<>("Target Orientation Time", VariableUnits.DURATION_MILLIS);
		
		/** The timestamp of when learners begin openly firing at the target */
		public static final ActorVariables<Long> OPEN_TIME = new ActorVariables<>("Open Time", VariableUnits.TIMESTAMP_MILLIS);
		
		/** The timestamp of when a target was hit for the first time */
		public static final ActorVariables<Long> TARGET_FIRST_STRIKE = new ActorVariables<>("Target First Strike", VariableUnits.TIMESTAMP_MILLIS);
		
		/** The amount of time it takes to eliminate a target after it is exposed */
        public static final ActorVariables<Long> KILL_EFFICIENCY = new ActorVariables<>("Kill Efficiency", VariableUnits.DURATION_MILLIS);
		
		/** The amount of time it takes to eliminate a target after open fire has started.
		 * Mutually exclusive with {@link #NO_KILL_TIME} */
		public static final ActorVariables<Long> KILL_TIME = new ActorVariables<>("Kill Time", VariableUnits.DURATION_MILLIS);
		
		/** The amount of time it takes learners to FAIL to eliminate a target after open fire has started. 
		 * Mutually exclusive with {@link #KILL_TIME} */
        public static final ActorVariables<Long> NO_KILL_TIME = new ActorVariables<>("No Kill Time", VariableUnits.DURATION_MILLIS);
		
		/** The timestamp of when learners stopped shooting at a target without eliminating it to fire at a different one*/
		public static final ActorVariables<Long> TARGET_CLOSE_TIME = new ActorVariables<>("Close Time", VariableUnits.TIMESTAMP_MILLIS);
		
		/** Amount of time that learners took to shoot the last target that they were tasked with shooting */
		public static final ActorVariables<Long> TARGET_ENGAGEMENT_TIME = new ActorVariables<>("Target Engagement Time", VariableUnits.DURATION_MILLIS);
		
		/** Timestamp when an observed assessment occurs */
		public static final ActorVariables<Long> OBSERVED_ASSESSMENT = new ActorVariables<>("Observed Assessment", VariableUnits.TIMESTAMP_MILLIS);
		
		/** The unique name of this variable */
		private String name;
		
		/** The units that this variable uses to measure its value */
		private VariableUnits<T> units;
		
		/**
		 * Creates a new actor-specific variable with the given name and units
		 * 
		 * @param displayName the unique name of the variable. Cannot be null.
		 * @param units the units that this variable uses to measure its value. Cannot be null.
		 */
		private ActorVariables(String name, VariableUnits<T> units) {
			
			if(name == null) {
				throw new IllegalArgumentException("The name of an actor variable cannot be null");
			} else if(units == null) {
				throw new IllegalArgumentException("The units of an actor variable cannot be null");
			}
			
			this.name = name;
			this.units = units;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public VariableUnits<T> getUnits() {
			return units;
		}
	}
	
	/**
	 * An abstract representation of a composite key that can uniquely identify a variable. Sometimes
	 * the name of a variable isn't enough to uniquely identify it, such as in the case of actor-specific
	 * variables that can have multiple actors with the same variable. Composite variable keys are used 
	 * to handle these cases and ensure uniqueness even when additional context surrounds a variable.
	 * 
	 * @author nroberts
	 *
	 * @param <T>
	 */
	private static abstract class AbstractVariableKey<T>{
		
		/** The variable definition */
		private VariableName<T> variableName;
		
		/**
		 * Creates the bare-minimum representation of a variable key, which only includes
		 * its variable definition
		 * 
		 * @param variableName the unique definition for the variable. Cannot be null.
		 */
		protected AbstractVariableKey(VariableName<T> variableName) {
			if(variableName == null) {
				throw new IllegalArgumentException("The variable definition for a variable key cannot be null");
			}
			
			this.variableName = variableName;
		}
		
		/**
		 * Gets a JSON representation of all of the contextual information stored
		 * between a variable key and its associated value. Subclasses are expected
		 * to provide any additional context beyond the bare minimum that's needed
		 * to satisfy the variable key contract.
		 * 
		 * @param value the value to populate into the variable's JSON representation
		 * @return the JSON representation of the variable. Will not be null.
		 */
		@SuppressWarnings("unchecked")
		public JSONObject printToJSON(Object value) {
			JSONObject varObject = new JSONObject();
			varObject.put("name", variableName.getName());
			varObject.put("units", variableName.getUnits().getName());
			varObject.put("value", value);
			varObject.put("context", generateContextJSON());
			
			return varObject;
		}
		
		/**
		 * Gets a JSON representation of any additional contextual information that helps
		 * identify this variable, such as the name of an actor for an actor-specific variable.
		 * 
		 * @return the JSON representation of the context. Cannot be null.
		 */
		abstract JSONObject generateContextJSON();

		@Override
		public int hashCode() {
			return Objects.hash(variableName);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AbstractVariableKey<?> other = (AbstractVariableKey<?>) obj;
			return Objects.equals(variableName, other.variableName);
		}
	}
	
	/**
	 * A composite key that uniquely identifies an actor variable. Both the name of the variable
	 * and the identifier associated with the actor are used to create this key.
	 * 
	 * @author nroberts
	 *
	 * @param <T> the type of value expected by the variable.
	 * @param <U> the type of identifier that represent's the variable's actor.
	 */
	private static class ActorVariableKey<T, U> extends AbstractVariableKey<T>{
		
		/** The actor that the variable is specific to */
		private AbstractAssessmentActor<U> actor;

		/**
		 * Creates a new composite key for the given variable that is specific to the given actor.
		 * 
		 * @param variableName the name of the actor-specific variable. Cannot be null.
		 * @param actor the actor to which the variable applies. Cannot be null.
		 */
		public ActorVariableKey(ActorVariables<T> variableName, AbstractAssessmentActor<U> actor) {
			super(variableName);
			
			if(actor == null) {
				throw new IllegalArgumentException("The actor of an actor state cannot be null");
			}
			
			this.actor = actor;
		}

		@SuppressWarnings("unchecked")
		@Override
		JSONObject generateContextJSON() {
			JSONObject context = new JSONObject();
			context.put("actor", actor.toString());
			
			return context;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + Objects.hash(actor);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			ActorVariableKey<?,?> other = (ActorVariableKey<?,?>) obj;
			return Objects.equals(actor, other.actor);
		}
	}
}
