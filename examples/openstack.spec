//---------------------------------------------------------------------------------
// Open stack API
//---------------------------------------------------------------------------------

type Error = {
    error: {
        @get code: int,
        @get title: string,
        @get message: string
    }
}

//---------------------------------------------------------------------------------
// Token service
//---------------------------------------------------------------------------------

type Authentication = {
    auth: {
        passwordCredentials: { @set username: string, @set password: string },
        @set tenantName: string?
    }
}

type Token = {
    access: {
        token: { issued_at: string, expires: string, @get id: string }
    },

    virtual 'X-Auth-Token' = [<access.token.id>]
}

type AuthToken = {
    'X-Auth-Token': string
}

type Belongs = {
    @set(value) belongsTo: string?
}

type TokenId = {
    @set(value) tokenId: string
}

type Endpoints = {
    @get endpoints: { name: string, adminURL: string  }*
}

service tokens [v2.0/tokens] {
    authenticate: Authentication => Token or Error = POST BODY[Authentication]
    validate: TokenId, Belongs => Token or Error = GET[<tokenId>?<belongsTo>]
    validateOnly: TokenId, Belongs => {} or Error = HEAD[<tokenId>?<belongsTo>]
    endpoints: Token, TokenId => Endpoints or Error = GET[<tokenId>/endpoints] HEADER[AuthToken]
}

//---------------------------------------------------------------------------------

client openStackRest provides tokens
