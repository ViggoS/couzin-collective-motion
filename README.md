# Simulation of Collective Motion

Course Project – Simulation of Complex Systems (FFR120), Chalmers University of Technology

![](https://github.com/ViggoS/couzin-collective-motion/blob/main/simulation_1type.gif)

### Overview

This repository contains the project work developed as part of the course Simulation of Complex Systems (FFR120) at Chalmers University of Technology. The aim of the project was to study and simulate collective motion as a complex system, inspired by the seminal work of Couzin et al. (2005) on self-organized animal group behavior.

Collective motion emerges from local interaction rules between individuals and often gives rise to rich, non-linear, and difficult-to-reproduce dynamics. This project explores how simple behavioral rules—such as attraction, repulsion, and alignment—can lead to global patterns such as schooling, flocking, or milling.

### Project Description

The project implements an agent-based model where each agent follows local interaction rules based on the relative positions and orientations of its neighbors. By varying key parameters (e.g. interaction radii, noise, and relative rule weights), the system exhibits a range of collective behaviors.

A central challenge addressed in this project is the sensitivity of complex systems to parameter choices and implementation details, which makes exact reproduction of published results difficult. While the simulations qualitatively reproduce several of the collective regimes reported by Couzin et al., quantitative agreement is not always achieved—highlighting the inherent complexity and stochasticity of the system.

The simulations are implemented using Processing, allowing for real-time visualization and interactive experimentation.

### Requirements

To run the simulations locally, you need:

- Processing (version 4.x recommended)
Download from: https://processing.org/download

- A system capable of real-time graphical rendering (standard laptop is sufficient)

No additional libraries beyond standard Processing are required.

### How to Run the Simulation

Install Processing. I would highly recommend installing and using the Processing extension in VS code also.  

Clone this repository:

```bash
git clone https://github.com/ViggoS/couzin-collective-motion.git
```

Open Processing or VS Code with the Processing extension.

Navigate to the project folder and open couzin_model/couzin_model.pde

### Learning Outcomes

Through this project, the following aspects of complex systems were explored:

- Emergence from local interaction rules

- Sensitivity to parameters and initial conditions

- Challenges in reproducing results from the literature

- Agent-based modeling and visualization


Click Run to start the simulation.

Simulation parameters (such as number of agents, interaction radius, and noise levels) can be adjusted directly in the source code to explore different dynamics.

### References

Couzin, I. D., Krause, J., Franks, N. R., & Levin, S. A. (2005).
Effective leadership and decision-making in animal groups.
Nature, 433(7025), 513–516.
