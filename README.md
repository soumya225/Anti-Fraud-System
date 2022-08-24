### A simple anti-fraud system

#### Role model
|                                 | Anonymous | MERCHANT | ADMINISTRATOR | SUPPORT |
|---------------------------------|-----------|----------|---------------|---------|
| POST /api/auth/user             | +         | +        | +             | +       |
| DELETE /api/auth/user           | -         | -        | +             | -       |
| PUT /api/auth/role              | -         | -        | +             | -       |
| PUT /api/auth/access            | -         | -        | +             | -       |
| GET /api/auth/list              | -         | -        | +             | +       |
| POST /api/antifraud/transaction | -         | +        | -             | -       |
| /api/antifraud/suspicious-ip    | -         | -        | -             | +       |
| /api/antifraud/stolencard       | -         | -        | -             | +       |
| GET /api/antifraud/history      | -         | -        | -             | +       |
| PUT /api/antifraud/transaction  | -         | -        | -             | +       |


#### POST /api/auth/user
Accepts the following JSON body:
```json
{
   "name": "<String value, not empty>",
   "username": "<String value, not empty>",
   "password": "<String value, not empty>"
}
```

Returns:
```json
{
   "id": <Long value, not empty>,
   "name": "<String value, not empty>",
   "username": "<String value, not empty>",
   "role": "<String value, not empty>"
}
```

#### DELETE /api/auth/user/{username}
Deletes the specified user.

#### GET /api/auth/list
Returns an array of objects representing the users.

#### PUT /api/auth/role 
Changes user roles. Accepts the following JSON body:
```json
{
    "username": "<String value, not empty>",
    "role": "<String value, not empty>"
}
```

#### PUT /api/auth/access
Locks/unlocks users.

```json
{
   "username": "<String value, not empty>",
   "operation": "<[LOCK, UNLOCK]>" 
}
```

#### POST /api/antifraud/suspicious-ip
Saves suspicious IP addresses to the database. Accepts the following JSON body:
```json
{
   "ip": "<String value, not empty>"
}
```

Returns
```json
{
   "id": "<Long value, not empty>",
   "ip": "<String value, not empty>"
}
```

#### DELETE /api/antifraud/suspicious-ip/{ip}
Deletes specified IP address from database. 

#### GET /api/antifraud/suspicious-ip
Shows all IP addresses in the database.

#### POST /api/antifraud/stolencard
Saves stolen card data in the database. It must accept the following JSON body:
```json
{
  "number": "<String value, not empty>"
}
```

Returns
```json
{
   "id": "<Long value, not empty>",
   "number": "<String value, not empty>"
}
```

#### DELETE /api/antifraud/stolencard/{number}
Deletes specified card number from the database.

#### GET /api/antifraud/stolencard
Shows card numbers stored in the database.

#### POST /api/antifraud/transaction
Accepts the following JSON body:
```json
{
  "amount": <Long>,
  "ip": "<String value, not empty>",
  "number": "<String value, not empty>",
  "region": "<String value, not empty>",
  "date": "yyyy-MM-ddTHH:mm:ss"
}
```
Number should be valid card number that passes the Luhn algorithm. 
Region should be a code from the following table.

| **Code** | **Description**                  |
|----------|----------------------------------|
| EAP      | East Asia and Pacific            |
| ECA      | Europe and Central Asia          |
| HIC      | High-Income countries            |
| LAC      | Latin America and the Caribbean  |
| MENA     | The Middle East and North Africa |
| SA       | South Asia                       |
| SSA      | Sub-Saharan Africa               |

If successful returns:
```json
{
  "result": <String>,
  "info": <String>
}
```
Result can be ALLOWED, MANUAL_PROCESSING, or PROHIBITED. 
The result for a card number depends on the number of different regions
of the transaction in the past hour, the number of unique IP addresses of the transaction
in the past hour, the amount of the transaction, and whether the number/IP has been blacklisted. In the case of not allowed, the info
field contains the reason for rejection (e.g. amount, card-number, ip, ip-correlation, region-correlation).

#### PUT /api/antifraud/transaction
Adds feedback for a transaction. Accepts the following JSON body. 
```json
{
   "transactionId": <Long>,
   "feedback": "<String>"
}
```

If successful, the limits of transaction validation are updated
according to a rule as described in the table below.

| Transaction Feedback → Transaction Validity ↓ |           ALLOWED          | MANUAL_PROCESSING |         PROHIBITED         |
|:---------------------------------------------:|:--------------------------:|:-----------------:|:--------------------------:|
|                    ALLOWED                    | Exception                  | ↓ max ALLOWED     | ↓ max ALLOWED ↓ max MANUAL |
|               MANUAL_PROCESSING               | ↑ max ALLOWED              | Exception         | ↓ max MANUAL               |
|                  PROHIBITED                   | ↑ max ALLOWED ↑ max MANUAL | ↑ max MANUAL      | Exception                  |

Responds with the following. 
```json
{
  "transactionId": <Long>,
  "amount": <Long>,
  "ip": "<String value, not empty>",
  "number": "<String value, not empty>",
  "region": "<String value, not empty>",
  "date": "yyyy-MM-ddTHH:mm:ss",
  "result": "<String>",
  "feedback": "<String>"
}
```

#### GET /api/antifraud/history
Shows transaction history - returns an array of JSON objects that represent
transactions. 

#### GET /api/antifraud/history/{number}
Shows the transaction history for a specified card number. 
