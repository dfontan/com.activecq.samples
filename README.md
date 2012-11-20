# Adobe CQ Samples

ActiveCQ's com.activecq.samples is a collection of sample implemenations and stubs for common Adobe CQ implemenation pieces.

At this time the Samples do not have test coverage and should be used as simple guides to help you understand the general approach for implemenation these various modules.

The Samples also include common tasks and comments describing the role of various aspects of the implementations.

__*If errors, corrections or enhancements are discovered, please open a github issue or issue a pull request*__

## Event Handlers

### JCR Event Listener/Observation Manager

- **JCR Event Listener** - Listen for and respond to JCR Events

### Sling Event Publisher/Event Listener

- **Sling Event Publisher** - Listen for Sling Events and create Custom Events
- **Sling Custom Event Handler** - Listen for and respond to Custom Events

## Live Actions

- **MSM Rollout Live Action** - Custom behavior to be executed during custom MSM Rollout Configs

## Login Module

- **Login Module** - Jackrabbit Login Module to authenticate credentials (passed in from a Sling Authentication Handler) and perform other log in related activities (creating CRX accounts, syncing data from external auth systems, etc.)

## Servlets

- **Sling All Methods Servlet** - Servlet implementation that responds to all HTTP methods. These are primary used to surface endpoints that respond to POST (and GET) Requests.

- **Sling Safe Methods Servlet** - Servlet implementation that responds to "Safe" HTTP Requests. These are primary used to surface endpoints that respond to GET Requests.

## Sling Adapter Factory

- **Sling Adapter Factory** - Factory which adapts (via `.adaptTo(…)`) from one class to another.

- **Sling Adaptable** - Allows classes to hook into custom Sling Adapter Factories.

## Sling Authentication Handler

- **Sling Authentication Handler** - Mechanism for requesting credentials from the client, extracting credentials from the HTTP Request, and dropping credentials from the HTTP Request.

## Sling Filters

- **Sling Filter** - Sling implementation of a Java Servlet Filter. Sling Filters have access to authenticated SlingHttpServletRequest objects (authentication status, target resource, etc.)

## Sling Scheduler

- ** Sling Scheduled Service ** - Run Jobs at a defined interval (ex. Every day at Midnight or every 15 minutes)

## Sling Resource Provider

- ** Sling Resource Provider ** - Expose non-CRX data as Sling Resources

## Sling Service

- **Sling Service** - Standard implementation of a Sling Service.
- **Sling Cluster Aware Service** - Sling service that can tell if it is being executed on a Master of Slave node in the cluster.

## Workflow

- **Workflow Process** - Standard work flow process step. Includes implementations for persisting state between workflow steps.
