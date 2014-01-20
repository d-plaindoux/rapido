type Authentication = { auth:{ passwordCredentials: { username:string, password:string }, tenantName:string? } }
type Token = { access:{ token: { issued_at:string, expires:string, id:string } } }
type Error = { error:{ code:int, title:string, message:string } }

type AuthToken = { 'X-Auth-Token':string }
type Endpoints = { endpoints:{ name:string, adminURL:string }* }
type Users = { users: { name: string }* }

service keystone {
    authenticate: Authentication => Token or Error = POST BODY[Authentication]
 }

service keystoneClient {
    endpoints: => Endpoints or Error = GET[tokens/<access.token.id>/endpoints] HEADER[AuthToken]
    tenants: => Tenants or Error = GET[tenants] HEADER[AuthToken]
 }

route keystone [v2.0/tokens]
route keystoneClient(token:Token, authToken: AuthToken) [v2.0]

client openStackRest provides keystone, keystoneClient
