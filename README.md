# Martini Core

## Table of Contents
1. [What is Martini?](#what)
	1. [What is Martini Core?](#what-core)
1. [How does Martini work?](#how)
	1. [Spring Framework](#how-spring)
1. [Who uses Martini?](#who)
	1. [Who uses Martini Core](#who-core)
1. [What is Martini's Goal?](#goal)
	1. [How can your organization benefit from using Martini?](#goal-benefits)
1. [Who develops and maintains Martini?](#ownership)
	1. [Recognition](#thanks)
1. [More Information](#info)


### What is Martini? <a name="what"></a>
Martini is an open-source [Behavior Driven Development (BDD)](https://www.agilealliance.org/glossary/bdd) testing 
framework for Java, similar in concept to Cucumber or JBehave. Martini provides multithreading, scenario lifecycle 
management and flexible filtering by leveraging the Spring Framework.

#### What is Martini Core? <a name="what-core"></a>
This is the library which provides interfaces and default implementations supporting feature, scenario 
and step definitions as well as filtering, categorization and gating (throttling) concepts.

### How does Martini Core work? <a name="how"></a>
The library leverages the power of the [Spring Framework](https://spring.io/projects/spring-framework).

#### It uses Spring to <a name="how-spring"></a>
* find .feature files using a ResourceResolver
* identify step implementation classes with an extension of the @Component annotation
* instantiate singleton step objects using BeanPostProcessor
* resolve step methods using a MethodResolver
* filter features and scenarios using Spring Expression Language (SpEL)
* manage scenario lifecycles using a Scope
* publish scenario lifecycle events using the @EventListener annotation
* publish suite lifecycle events using the @EventListener annotation


### Who uses Martini? <a name="who"></a>
Martini is designed to ensure software quality through use by an [Agile](https://www.agilealliance.org/agile101/) 
team's [Three Amigos](https://www.agilealliance.org/glossary/three-amigos). The Three Amigos
generally consists of at least a Product Owner, a Software Engineer and a Quality Assurance 
Automation Engineer or Software Design Engineer in Test (SDET).

The Amigos should agree on feature specifications, then capture these specifications in 
[Gherkin](http://toolsqa.com/cucumber/gherkin/) as scenarios describing expected behavior.


#### Who uses Martini Core? <a name="who-core"></a>
Martini Core is intended to be used by a Quality Assurance Automation Engineer with fluency in 
the Java programming language and familiarity with the Spring Framework. The engineer uses Martini Core to
implement executable steps in a scenario.


### What is Martini's goal? <a name="goal"></a>
Martini was designed to be an enterprise-level tool, solving fundamental problems encountered with other BDD 
frameworks.

#### How can your organization benefit from using Martini? <a name="goal-benefits"></a>
* Martini tests are written in Java and leverage Spring, and as a result can be relatively easily and 
quickly implemented and understood by most Java programmers.

* Other libraries may be incorporated into a Martini suite. For example, [Selenium](https://www.seleniumhq.org/)
may be used to launch a browser for each scenario testing a web application feature.

* By default, Martini executes scenarios in parallel. Test suites executed on a multi-CPU system will complete much
faster than in a suite written for a single-threaded virtual machine, such as with Ruby or Python.

* As Martini executes in parallel, application deadlock issues may be detected through regular testing.

* Martini suites can be executed in cloud environments. 

* Martini has a first-class concept of hierarchical test categories, useful for subsystem test failure
reporting and selective test filtering. 

* Martini leverages Spring Expression Language, allowing for nested boolean filtering of test scenario
execution.

* Martini provides the capability filtering based on test category, test feature, individual scenario or 
even resource location.

* Martini functional test suites may be leveraged with use of the Martini JMeter plugin.

* Resource-intensive scenarios may be gated, preventing crashes of the application under test.


### Who develops and maintains Martini? <a name="ownership"></a>

Martini was developed by Penny Rohr Curich through Digital Measures' sabbatical program in the Spring
of 2017 as the organic result of discussions regarding automation testing improvement by QA team peers.

Martini continues to be enhanced by Penny through 2018 to accomodate dependency release updates and as
Digital Measures by Watermark's QA needs arise.

Requests for outside contribution are welcome!

#### Recognitions from Penny <a name="thanks"></a>
* A big thank you to [Aslak Hellesøy](http://aslakhellesoy.com/) et al, for creating, releasing and 
maintaining Cucumber.
* Thank you to [Rod Johnson](https://en.wikipedia.org/wiki/Rod_Johnson_(programmer)) et al, for creating, releasing 
and maintaining Spring.
* Thank you to [Cedric Beust](https://beust.com), for all the great Java contributions over the years.
* Thank you to [Digital Measures](https://www.digitalmeasures.com/) CEO Matt Bartel and CTO Michael Rentas for 
sponsoring employee sabbaticals.
* Thank you to friends at Spotify (you know who you are) for the open-source encouragement. "**The worst thing
that can happen is that nobody pays any attention.**"
* Thank you to Ethan Jahns, formerly of Digital Measures, for soundboarding ideas and for forking an 
improved Gherkin parser.
* Thank you to John Kieffer and Benjamin Newman at Digital Measures for encouragement.


### Where can I find more information on Martini? <a name="info"></a>

#### In Progress: [Martini Core Wiki](https://github.com/qas-guru/martini-core/wiki) <a name="wiki"></a>

#### In Progress: [__Martini - swank software testing in Java__](https://leanpub.com/martini) <a name="book"></a>