# RiistaSDK

A document containing reasoning why SDK has been implemented as it was.

## Design decisions

### Networking

Don't use automatic redirect following provided by Ktor since on iOS the HttpRedirect library doesn't switch to GET method if the initial request method was POST. There is a HttpRedirect fork that would allow switching the method but that doesn't strip request body (GET requests are not allowed to have body on iOS).

### Database

We're using same database for all users. Therefore table primary keys must take into account that usernames may change
and thus entity remote ids cannot solely be used as primary key.
