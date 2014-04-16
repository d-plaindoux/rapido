/*
 * Copyright (C)2014 D. Plaindoux.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; see the file COPYING.  If not, write to
 * the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package @OPT[|@USE::package.|]core

// ---------------------------------------------------------------------------------------------------------------------

import "encoding/json"
import "fmt"
import "strings"
import "errors"

// ---------------------------------------------------------------------------------------------------------------------
// JSon type definition
// ---------------------------------------------------------------------------------------------------------------------

type toString func(JSon)string

type JSon interface {
    JSonType()
    String() string
    ToJSonString() string
    RawValue() interface{}
    SetValue(path []string, value JSon) JSon
    Overrides(data JSon) JSon
    overridden(data Map) JSon
}

// ---------------------------------------------------------------------------------------------------------------------
// Abstract JSon type / Private class
// ---------------------------------------------------------------------------------------------------------------------

type abstractJSon struct {}
func (abstractJSon) JSonType() {}
func (abstractJSon) String { return "" }
func (abstractJSon) ToJSonString { return "" }
func (abstractJSon) RawValue { return nil }
func (this abstractJSon) SetValue(path []string, value JSon) JSon {
    result := value
    for i := len(path); i > 0; i-- {
        result = NewMap(map[string]JSon{ path[i-1] : result })
    }
    return result.Overrides(this)
}
func (this abstractJSon) Overrides(data JSon) JSon {
    return this
}
func (this abstractJSon) overridden(data Map) JSon {
    return data
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
func (this Number) ToJSonString() string {
    return this.String()
}
func (this Number) RawValue() interface{} {
    return this.value
}

// ---------------------------------------------------------------------------------------------------------------------

func NewNumber(value float64) *JSon {
    o := new(Number)
    o.value = value
    return o
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

// ---------------------------------------------------------------------------------------------------------------------

func NewString(value string) *JSon {
     o := new(String)
     o.value = value
     return o
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
func (this Boolean) ToJSonString() string {
    return this.String()
}
func (this Boolean) RawValue() interface{} {
    return this.value
}

// ---------------------------------------------------------------------------------------------------------------------

func NewBoolean(value bool) *JSon {
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
func (this Null) ToJSonString() string {
    return this.String()
}
func (this Null) RawValue() interface{} {
    return nil
}

// ---------------------------------------------------------------------------------------------------------------------

func NewNull() *JSon {
    return new(Null)
}

// ---------------------------------------------------------------------------------------------------------------------
// Array JSon type
// ---------------------------------------------------------------------------------------------------------------------

type Array struct {
	abstractJSon
	value []JSon
}
func (this Array) toString(f toString) string {
    var data = make([]string, len(this.value))
    for k := range this.value {
        data[k] = f(this.value[k])
    }
    return fmt.Sprintf("[ %s ]", strings.Join(data, ", "))
}
func (this Array) String() string {
    return this.toString(func(value JSon)string{return value.String()})
}
func (this Array) ToJSonString() string {
    return this.toString(func(value JSon)string{return value.ToJSonString()})
}
func (this Array) RawValue() interface{} {
    var data []interface{}
    for k := range this.value {
        data = append(data, this.value[k].RawValue())
    }
    return data
}

// ---------------------------------------------------------------------------------------------------------------------

func NewArray(value []JSon) *JSon {
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
func (this Map) toString(f toString) string {
    var data []string
    for k := range this.value {
        data = append(data, fmt.Sprintf("\"%s\": %s",k,f(this.value[k])))
    }
    return fmt.Sprintf("{%s}", strings.Join(data, ","))
}
func (this Map) String() string {
    return this.toString(func(j JSon)string{return j.String()})
}
func (this Map) ToJSonString() string {
    return this.toString(func(j JSon)string{return j.ToJSonString()})
}
func (this Map) RawValue() interface{} {
    var data = make(map[string]interface{})
    for k := range this.value {
        data[k] = this.value[k].RawValue()
    }
    return data
}
func (this Map) Overrides(data JSon) JSon {
    return data.overridden(this)
}
func (this Map) overridden(data Map) JSon {
    result := make([string]JSon)
    for k := range this.values {
      if value, found := data.values[k]; found {
        result[k] = value.Overrides(this.value[k])
      } else {
        result[k] = this.values[k]
      }
    }
    for k := range data.values {
      if value, found := this.values[k]; found {
        result[k] = data.values[k].Overrides(value)
      } else {
        result[k] = data.values[k]
      }
    }
    return result
}

// ---------------------------------------------------------------------------------------------------------------------

func NewMap(value map[string]JSon) *JSon {
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
           if result,error := ValueOfJSon(e[v]); error == nil {
            data[v] = result
           } else {
            return nil, error
           }
        }
        return NewArray(data), nil
    case map[string]interface{}:
        var data = make(map[string]JSon)
        for v := range e {
           if result,error := ValueOfJSon(e[v]); error == nil {
            data[v] = result
           } else {
            return nil, error
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

    if error := json.Unmarshal([]byte(s),&v); error == nil {
	return ValueOfJSon(v)
    } else {
	return nil, error
    }
}

func main() {
    if json,error := StringOfJSon(`["a",12,true,{"a":12}]`); error == nil {
	fmt.Println(json.ToJSonString())
    } else {
	fmt.Println(error)
    }
    m := NewMap(make(map[string]JSon))
    fmt.Println(m.SetValue([]string{"a","b"}, NewString("test")))
}
