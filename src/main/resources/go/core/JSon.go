package @OPT[|@USE::package.|]core

// ---------------------------------------------------------------------------------------------------------------------

import "encoding/json"
import "fmt"
import "strings"
import "errors"

// ---------------------------------------------------------------------------------------------------------------------
// JSon type definition
// ---------------------------------------------------------------------------------------------------------------------

type JSon interface {
    isJSon()
    String() string
    ToJSonString() string
    RawValue() interface{}
    SetValue(path []string, value JSon) JSon
}

// ---------------------------------------------------------------------------------------------------------------------
// Abstract JSon type
// ---------------------------------------------------------------------------------------------------------------------

type abstractJSon struct {}
func (abstractJSon) isJSon() {}
func (this abstractJSon) String() string {
    return "" // TODO manage error case in a better way
}
func (this abstractJSon) ToJSonString() string {
    return this.String()
}
func (this abstractJSon) SetValue(path []string, value JSon) JSon {
    return value
}

// ---------------------------------------------------------------------------------------------------------------------
// Number JSon type
// ---------------------------------------------------------------------------------------------------------------------

type Number struct {
	abstractJSon
	value float64
}
func (this Number) String() string {
    return fmt.Sprintf("%f", this.value)
}
func (this Number) RawValue() interface{} {
    return this.value
}
func NewNumber(value float64) JSon {
    o := new(Number)
    o.value = value
    return *o
}
// ---------------------------------------------------------------------------------------------------------------------
// String JSon type
// ---------------------------------------------------------------------------------------------------------------------

type String struct {
	abstractJSon
	value string
}
func (this String) String() string {
    return this.value
}
func (this String) ToJSonString() string {
    return fmt.Sprintf("\"%s\"", this.value)
}
func (this String) RawValue() interface{} {
    return this.value
}
func NewString(value string) JSon {
     o := new(String)
     o.value = value
     return *o
}

// ---------------------------------------------------------------------------------------------------------------------
// Boolean JSon type
// ---------------------------------------------------------------------------------------------------------------------

type Boolean struct {
	abstractJSon
	value bool
}
func (this Boolean) String() string {
    if this.value {
        return "true"
    } else {
        return "false"
    }
}
func (this Boolean) RawValue() interface{} {
    return this.value
}
func NewBoolean(value bool) JSon {
    o := new(Boolean)
    o.value = value
    return o
}

// ---------------------------------------------------------------------------------------------------------------------
// Null JSon type
// ---------------------------------------------------------------------------------------------------------------------

type Null struct { abstractJSon }
func (this Null) String() string {
    return "null"
}
func (this Null) RawValue() interface{} {
    return nil
}
func NewNull() JSon {
    return *new (Null)
}

// ---------------------------------------------------------------------------------------------------------------------
// Array JSon type
// ---------------------------------------------------------------------------------------------------------------------

type Array struct {
	abstractJSon
	value []JSon
}
func (this Array) String() string {
    var data = make([]string, len(this.value))
    for k := range this.value {
        data[k] = this.value[k].String()
    }
    return fmt.Sprintf("[ %s ]", strings.Join(data, ", "))
}
func (this Array) RawValue() interface{} {
    var data []interface{}
    for k := range this.value {
        data = append(data, this.value[k].RawValue())
    }
    return data
}
func NewArray(value []JSon) JSon {
     o := new(Array)
     o.value = value
     return o
}

// ---------------------------------------------------------------------------------------------------------------------
// Map JSon type
// ---------------------------------------------------------------------------------------------------------------------

type Map struct {
	abstractJSon
	value map[string]JSon
}
func (this Map) String() string {
    var data []string
    for k := range this.value {
        data = append(data, fmt.Sprintf("\"%s\": %s",this.value[k].String()))
    }
    return fmt.Sprintf("{ %s }", strings.Join(data, ", "))
}
func (this Map) RawValue() interface{} {
    var data = map[string]interface{}{}
    for k := range this.value {
        data[k] = this.value[k].RawValue()
    }
    return data
}
func NewMap(value map[string]JSon) JSon {
    o := new(Map)
    o.value = value
    return o
}

// ---------------------------------------------------------------------------------------------------------------------
// JSon conversion function
// ---------------------------------------------------------------------------------------------------------------------

func ValueOfJSon(v interface{}) (JSon,error) {
    switch e := v.(type) {
    case int:
        return NewNumber(float64(e)), nil
    case float64:
        return NewNumber(e), nil
    case string:
        return NewString(e), nil
    case bool:
        return NewBoolean(e), nil
    case []interface{}:
        var data = make([]JSon, len(e))
        for v := range e {
           result,error := ValueOfJSon(e[v])
           if (error != nil) {
            return nil, error
           } else {
            data[v] = result
           }
        }
        return NewArray(data), nil
    case map[string]interface{}:
        var data map[string]JSon
        for v := range e {
           result,error := ValueOfJSon(e[v])
           if (error != nil) {
            return nil, error
           } else {
            data[v] = result
           }
        }
        return NewMap(data), nil
    case nil:
        return NewNull(), nil
    default:
        return nil, errors.New(fmt.Sprintf("Unexpected type %T while creating JSon data", e))
    }
}

func StringOfJSon(s string) (JSon,error) {
    var v interface{}
    error := json.Unmarshal([]byte(s),&v)
    if (error != nil) {
	return nil, error
    }
    return ValueOfJSon(v)
}
