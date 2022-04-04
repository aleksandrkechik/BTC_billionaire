# BTC_billionaire
This project uses single node by default, but I added a possibility to run it on multiple nodes.
The data still will be in sync if we will use same accountID on all of the nodeы (now it's stored in an application config).
For further scalability it's possible to implement Replicated Event Sourcing. Another thing that can be implemented is dividing 
main actor and endpoint. They can be transported to different microservices connected via RabbitMQ.
