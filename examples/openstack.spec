type Authentication = {auth:{passwordCredentials: {username:string, password:string}}, tenantName:string?}
type Token = {access:{token: {issued_at:string, expires:string, id:string}}}
type Access = {access:Token }
type Error = {error:{ code:int, title:string, message:string}}

type AuthToken = { 'X-Auth-Token':string }
type Endpoints = { endpoints:{ name:string}* }

service keystone {
    authenticate: Authentication => Token or Error = GET BODY[Authentication]
    endpoints: { tokenId:string } with AuthToken => Endpoints = GET[<tokenId>/endpoints] HEADER[AuthToken]
}

route keystone [/v2.0/tokens]

client openStackRest provides keystone
