type Credential = { username:String, password:String, tenantName: String? }
type Authentication = {auth: {passwordCredentials: {username: String, password: String}}, tenantName: String?}
type Token = {access: {token: {issued_at: String, expires: String, id: String}}}
type Error = {error: { code:Int, title: String, message: String}}

service keystone {
    authenticate: Credential => Token or Error = GET BODY[Authentication]
}

route keystone [v2.0/tokens]

client openStackRest provides keystone
