package @OPT[|@USE::package.|]core

// ---------------------------------------------------------------------------------------------------------------------

import "encoding/json"
import "fmt"
import "strconv"
import "strings"

// ---------------------------------------------------------------------------------------------------------------------

type any interface{}

// ---------------------------------------------------------------------------------------------------------------------
// JSon type definition
// ---------------------------------------------------------------------------------------------------------------------

type JSon interface {
    isJSon()
    String() string
    RawValue() any
}

// ---------------------------------------------------------------------------------------------------------------------
// Number JSon type
// ---------------------------------------------------------------------------------------------------------------------

type Number struct { value int }
func (Number) isJSon() {}
func (this Number) String() string {
    return strconv.Itoa(this.value)
}
func (this Number) RawValue() any {
    return this.value
}

// ---------------------------------------------------------------------------------------------------------------------
// String JSon type
// ---------------------------------------------------------------------------------------------------------------------

type String struct { value string }
func (String) isJSon() {}
func (this String) String() string {
    return this.value
}
func (this String) RawValue() any {
    return fmt.Sprint("\"%s\"", this.value)
}

// ---------------------------------------------------------------------------------------------------------------------
// Boolean JSon type
// ---------------------------------------------------------------------------------------------------------------------

type Boolean struct { value bool }
func (Boolean) isJSon() {}
func (this Boolean) String() string {
    if this.value {
        return "true"
    } else {
        return "false"
    }
}
func (this Boolean) RawValue() any {
    return this.value
}

// ---------------------------------------------------------------------------------------------------------------------
// Null JSon type
// ---------------------------------------------------------------------------------------------------------------------

type Null struct { }
func (Null) isJSon() {}
func (this Null) String() string {
    return "null"
}
func (this Null) RawValue() any {
    return nil
}

// ---------------------------------------------------------------------------------------------------------------------
// Array JSon type
// ---------------------------------------------------------------------------------------------------------------------

type Array struct { value []JSon }
func (Array) isJSon() {}
func (this Array) String() string {
    var data = make([]string, len(this.value))
    for k := range this.value {
        data[k] = this.value[k].String()
    }
    return fmt.Sprintf("[ %s ]", strings.Join(data, ", "))
}
func (this Array) RawValue() any {
    var data []any
    for k := range this.value {
        data = append(data, this.value[k].RawValue())
    }
    return data
}

// ---------------------------------------------------------------------------------------------------------------------
// Map JSon type
// ---------------------------------------------------------------------------------------------------------------------

type Map struct { value map[string]JSon }
func (Map) isJSon() {}
func (this Map) String() string {
    var data []string
    for k := range this.value {
        data = append(data, fmt.Sprintf("\"%s\": %s",this.value[k].String()))
    }
    return fmt.Sprintf("[ %s ]", strings.Join(data, ", "))
}
func (this Map) RawValue() any {
    var data = map[string]any{}
    for k := range this.value {
        data[k] = this.value[k].RawValue()
    }
    return data
}

// ---------------------------------------------------------------------------------------------------------------------
// JSon conversion function
// ---------------------------------------------------------------------------------------------------------------------

func ValueOfJSon(v any) JSon {
    switch e := v.(type) {
    case int:
        return Number{e}
    case string:
        return String{e}
    case bool:
        return Boolean{e}
    case []any:
        var data = make([]JSon, len(e))
        for v := range e {
           data[v] = ValueOfJSon(e[v])
        }
        return Array{data}
    case map[string]any:
        var data map[string]JSon
            for v := range e {
           data[v] = ValueOfJSon(e[v])
        }
        return Map{data}
    }
    return Null{}
}

func StringOfJSon(v string) JSon {
    val v any
    json.Unmarshal([]byte(v),&v)
    return ValueOfJSon(v)
}

// TODO

func main() {
    json := ValueOfJSon("a")
    fmt.Printf(json.String())
}

