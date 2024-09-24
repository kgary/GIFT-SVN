
# Competency Overview Report

The "Competency Overview Report" focuses on developing a suite of experiential team training modules within a synthetic training environment, supported by the Generalized Intelligent Framework for Tutoring (GIFT). The project, a collaboration between the U.S. Army Soldier Center Simulation and Training Technology Center (STTC) and Arizona State University (ASU), aims to enhance the ability of individual team members to coordinate with the larger team. Below are some key points extracted from the report:

## Executive Summary
- **Objective:** To develop and evaluate team training modules for Tactical Combat Casualty Care within a synthetic environment, supported by GIFT.
- **Team Training Goal:** To assess and improve team coordination and performance in Casualty Collection Point (CCP) operations.
- **Measurement System:** Utilizes a layered dynamics approach to calculate system entropy, mutual information, and other metrics to evaluate team performance and influence.

## Plan Summary
- **Project Duration:** Three-year collaborative project.
- **Base Period:** Focuses on developing a functional prototype and collecting empirical evidence of the training material's potential.

## Measurement System Overview
- **Layer Dynamics Approach:** The system is divided into layers composed of components. Each component's state changes affect the overall system entropy.
- **Entropy Measurement:** Entropy is calculated using the probability of state occurrences over a moving time window, indicating system reorganization.
- **Metrics:** Percent determinism, frequency, average mutual information (AMI), and entropy over time are used to evaluate team performance measures such as relaxation time, adaptation, and resilience.

## Input Signals for Layers

### Casualty Layer
- **Components:** Triage Status, Stable, Getting Transported, Getting Treated.
- **States:** Represent the status and treatment of casualties, with specific coding for triage, stability, transportation, and treatment.

### Visual Activity Layer
- **Components:** AOI (Areas of Interest), Looking At.
- **States:** Represent trainees' gaze focus on AOIs and objects of interest.

### Movement Layer
- **Components:** Location, Heading.
- **States:** Represent trainees' physical location and movement direction.

### Environment Layer
- **Components:** Scenario Transitions, Perturbation.
- **States:** Represent fixed events based on time or scenarios.

### Communication Layer
- **Components:** Speaker, Directed Communication.
- **States:** Represent communication patterns among trainees, both non-directed and directed.
