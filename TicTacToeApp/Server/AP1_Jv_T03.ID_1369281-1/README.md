# Project 03 — Java_Bootcamp

**Summary:** In this project, you will learn how to create a web application in Java using Spring.

💡 *[Click here](https://new.oprosso.net/p/4cb31ec3f47a4596bc758ea1861fb624) to share your feedback on this project.* It's anonymous and will help our team improve the training. We recommend filling out the survey right after completing the project.

## Contents**

 1. [Chapter I](#chapter-i)   
     - [Instructions](#instructions)   
 2. [Chapter II](#chapter-ii)  
     - [General Information](#general-information)  
 3. [Chapter III](#chapter-iii)      
     - [Task 0. Creating the Project](#task-0-creating-the-project)  
     - [Task 1. Creating the Project Structure](#task-1-creating-the-project-structure)  
     - [Task 2. Implementing the Domain Layer](#task-2-implementing-the-domain-layer)  
     - [Task 3. Implementing the Datasource Layer](#task-3-implementing-the-datasource-layer)  
     - [Task 4. Implementing the Web Layer](#task-4-implementing-the-web-layer)  
     - [Task 5. Implementing the DI Layer](#task-5-implementing-the-di-layer) 

## Chapter I

### Instructions

1. Throughout the course, you will experience uncertainty and a severe lack of information — this is normal. Remember that the repository and Google are always available to you, as are your peers and Rocket.Chat. Communicate. Search. Rely on common sense. Do not be afraid of making mistakes.
1. Pay attention to sources of information. Verify, think, analyze, compare.
1. Read the assignments carefully. Reread them several times.
1. It's best to read the examples carefully as well. They may contain something not explicitly stated in the assignment itself.
1. You might encounter inconsistencies when something new in the task or example contradicts what you already know. If that happens, try to figure it out. If you fail, make a note under “open questions” and resolve it during your work. Do not leave open questions unresolved.
1. If a task seems unclear or unachievable, it only seems that way. Try decomposing it. Most likely, individual parts will become clearer.
1. Along the way, you'll encounter many different tasks. Those marked with an asterisk (\*) are for more meticulous learners. They are of higher complexity and are not mandatory, but if you do them, you'll gain additional experience and knowledge.
1. Do not try to fool the system or those around you. You'll only be fooling yourself.
1. Have a question? Ask the neighbor on your right. If that doesn't help, ask the neighbor on your left.
1. When using someone's help, always make sure you understand why, how, and what for. Otherwise, that help is meaningless.
1. Always push only to the **develop** branch! The **master** branch will be ignored. Work in the **src** directory.
1. Your directory should not contain any files other than those specified in the tasks.

## Chapter II

### General Information

A web application is a client-server application in which the client interacts with a web server using a browser. The web application logic is distributed between the server and the client; data is primarily stored on the server, and information is exchanged over a network.

**Spring** is one of the most popular frameworks for creating web applications in Java. Advantages of Spring include:

- **Inversion of Control (IoC) and Dependency Injection (DI)**: Spring provides IoC and DI mechanisms that allow you to manage dependencies between application components. These mechanisms make code more modular, reduce coupling, and improve testability.
- **Modularity**: Spring consists of various modules such as Spring Core, Spring MVC, Spring Security, Spring Data, and more. This allows developers to select only what they need for their applications, making development lightweight and flexible.
- **Support for Aspect-Oriented Programming (AOP)**: Spring provides tools for working with aspects, allowing cross-cutting concerns (e.g., logging, transactions) to be separated from the main application logic, improving its modularity and maintainability.
- **Transaction Support**: Spring offers declarative transaction management, allowing you to manage transactions without direct calls to the data access layer APIs.
- **Easy Testing**: With DI and IoC, Spring-based applications are easier to test because dependencies can be replaced with mock objects for easier unit testing.
- **Support for Various Application Types**: Spring provides capabilities for developing a wide spectrum of applications, including web applications (Spring MVC), RESTful APIs (Spring WebFlux), microservices, data processing, database integration, and more.

**Topics to study:**

- Web application;
- Spring;
- API;
- The Minimax algorithm;
- MVC.

## Chapter III

**Project: Tic-Tac-Toe**

We will explore the basics of building web applications and using the Spring framework. The project is created once and used for all subsequent tasks.


### Task 0. Creating the Project

In IntelliJ IDEA, create a new project:

- Select Java as the language.
- Select Gradle as the build system.
- Select JDK 18; if you don't have it, download any version of JDK 18.
- Select Kotlin as the Gradle DSL.

### Task 1. Creating the Project Structure

- Each layer is a separate package.
- The project structure must include the following layers: web, domain, datasource, di.
- The web layer must contain at least the packages model, controller, mapper for interaction with the client.
- The domain layer must include at least the packages model, service for implementing the business logic of the application.
- The datasource layer must include at least the packages model, repository, mapper for implementing data operations (for example, working with a database).
- The di layer defines the dependency injection configurations.

### Task 2. Implementing the Domain Layer

- Describe the game board model as an integer matrix.
- Describe the model of the current game, which has a UUID and a game board.
- Describe the interface of a service that has the following methods:
  - A method to get the next move of the current game using the Minimax algorithm.
  - A method to validate the current game board (check that previous moves haven't been changed).
  - A method to check if the game has ended.
- Models, interfaces, and implementations must be in separate files.

### Task 3. Implementing the Datasource Layer

- Implement a storage class to store current games.
- Use thread-safe collections for data storage.
- Describe the models of the game board and the current game.
- Implement domain<->datasource mappers.
- Implement a repository for working with the storage class; it must have the following methods:
  - A method to save the current game.
  - A method to get the current game.
- Create a class that implements the service interface and accepts the repository (for working with the storage class) as a parameter.
- Models, interfaces, and implementations must be in separate files.

### Task 4. Implementing the Web Layer

- Describe the models of the game board and the current game.
- Implement domain<->web mappers.
- Implement a controller using Spring that has a POST /game/{UUID} method (where {UUID} is the UUID of the current game). This method receives the current game with an updated board from the user and returns the current game with the updated board for the computer's turn.
- If an invalid current game or updated board is sent, the method should return an error with a description.
- The application must support multiple games simultaneously.
- Models, interfaces, and implementations must be in separate files.

### Task 5. Implementing the DI Layer

- Implement a Spring Configuration class that describes the dependency graph.
- It must contain at least:
  - The storage class as a singleton;
  - A repository to work with the storage class.
  - A service to work with the repository.

