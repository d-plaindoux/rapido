//
// Study - object construction expression
//

type Authentication = { auth:{ passwordCredentials: { username:string, password:string }, tenantName:string? } }
type Token = { access:{ token: { issued_at:string, expires:string, id:string } } }
type Access = { access:Token }
type Error = { error:{ code:int, title:string, message:string } }

type AuthToken = { 'X-Auth-Token':string }
type Endpoints = { endpoints:{ name:string }* }

factory Token => AuthToken {
    access.token.id => 'X-Auth-Token'
 }

service client [/v2.0/tokens] {
    authenticate: Credential => keystone or Error = POST BODY[Authentication]
}

service keystone(Token) [/v2.0] {
    endpoints: => Endpoints = GET[<access.token.id>/endpoints] HEADER[AuthToken]
}

client openStackRest provides client
