# Pilotes of the great Miquel Montoro 
*Author: Maura Piredda*

The **Pilotes of the great Miquel Montoro** application allows to order the "pilotes", a Majorcan recipe consisting of a meatball stew, of the great Miquel Montoro.

The application exposes the following API

**Public**
* `GET /v1.0/orders/{number}`: returns the order identified by the order number
* `POST /v1.0/orders`: saves a new order
* `PUT /v1.0/orders`: updates an order. An order can be updated only if it has not been notified yet and if it is not too much old. See the *Configuration* section for more details.
* `POST /v1.0/auth/login`: returns a JWT. As POC, this operation always returns a JWT, no matter the passed username and password, but only the **"admin"** user is allowed to perform the secured operations.

**Secured**
* `POST /v1.0/searchByCustomer`: returns the list of the orders of a customer, allowing partial searches, e.g. it can returns all orders of customers whose name contains an a in their name.

Asynchronously, the orders are notified to an external service, that Miquel will use to receive the orders and start cooking.

### Configuration

The application can be configured through the `application.properties` file:

**Orders Configuration** 
`order.pilotes.unitaryPrice`: the price of one meet ball  
`order.pilotes.expirationMinutes`: the number of minutes after the order creation during which the order can be modified. After these minutes the order cannot be modified anymore.

**Orders Notifier Configuration** 
* `order.notifier.enabled`: enabled/disabled the orders notification
* `order.notifier.webhook.url`: the URL of the service that receives the orders to notify
* `order.notifier.webhook.apyKey`: the API key of the service that receives the orders to notify
* `order.notifier.webhook.timeout`: the timeout in seconds of the notification request

**Authentication**
* `pilotes.secret.key`: the secret key to generate the JWT. It must be encrypted or replaced by a keystore to be production ready
* `pilotes.jwt.expirationSeconds`: the validity time of a JWT 

### Build

To build and run test execute

```sh
$ mvn install
```

To build a docker image execute

```sh
$ mvn install -P docker-build
```