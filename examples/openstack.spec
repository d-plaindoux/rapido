type Authentication = {
    auth: {
        passwordCredentials: {
            @set username: string,
            @set password: string
        },
        @set tenantName: string?
    }
}

type Token = {
    access: {
        token: {
            issued_at: string,
            expires: string,
            @get id: string
        }
    }
}

type Error = {
    error: {
        @get code: int,
        @get title: string,
        @get message: string
    }
}

type AuthToken = {
    @{set,get}(token) 'X-Auth-Token': string
}

type Endpoints = {
    @get endpoints: {
        name: string,
        adminURL: string
    }*
}

type Tenants = {
    @get tenants: {
        name: string,
        id: string
    }*
}

service keystone [v2.0/tokens] {
    authenticate: Authentication => Token or Error = POST BODY[Authentication]
 }

service keystoneClient(Token, AuthToken) [v2.0] {
    endpoints: => Endpoints or Error = GET[tokens/<access.token.id>/endpoints] HEADER[AuthToken]
    tenants: => Tenants or Error = GET[tenants] HEADER[AuthToken]
 }

client openStackRest provides keystone, keystoneClient
