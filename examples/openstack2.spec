// Study - Expressivity for object construction

type Credential = {username:string, password:string, tenantName:string?}
type Authentication = {auth:{passwordCredentials: {username:string, password:string}, tenantName:string?}}
type Token = {access:{token: {issued_at:string, expires:string, id:string}}}
type Access = {access:Token }
type Error = {error:{ code:int, title:string, message:string}}

type AuthToken = { 'X-Auth-Token':string }
type Endpoints = { endpoints:{ name:string}* }

factory Authentication(Credential) {
    username => auth.passwordCredentials.username
    password => auth.passwordCredentials.password
    tenantName => auth.tenantName
}

factory AuthToken(Token) {
    access.token.id => 'X-Auth-Token'
}

service keystone(t:Token?) route [/v2.0/tokens] {
    authenticate: Credential => keystone(Token) or Error = POST BODY[Authentication]
    endpoints: => Endpoints = GET[<t.access.token.id>/endpoints] HEADER[AuthToken]
}

client openStackRest provides keystone
