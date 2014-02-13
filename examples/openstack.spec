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
        passwordCredentials: {
            @set username: string,
            @set password: string
        },
        @set tenantName: string?
    }
}

type Token = {
    access: { token: { id: string } }
}

type AuthToken = {
    virtual 'X-Auth-Token' = [<access.token.id>]
}

type Belongs = {
    @set(value) belongsTo: string?
}

type TokenId = {
    @set(id) tokenId: string
}

type Endpoints = {
    @get endpoints: { name: string, adminURL: string  }*
}

type Empty = {}

service tokens [v2.0] {
    authenticate: Authentication    => Token or Error       = POST[tokens] BODY[Authentication]
    validate    : TokenId, Belongs  => Token or Error       = GET[tokens/<tokenId>?<belongsTo>]
    validateOnly: TokenId, Belongs  => Empty or Error       = HEAD[tokens/<tokenId>?<belongsTo>]
    endpoints   : Token, TokenId    => Endpoints or Error   = GET[/tokens/<tokenId>/endpoints] HEADER[AuthToken]
}

//---------------------------------------------------------------------------------

client openStackRest provides tokens
