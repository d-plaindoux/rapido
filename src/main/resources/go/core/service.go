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

import "@OPT[|@USE::package.|]core.json"

// ---------------------------------------------------------------------------------------------------------------------
// HttpService handler specification
// ---------------------------------------------------------------------------------------------------------------------

type HttpService interface {
    Handle(servicePath string, operation string, params, body, header map[string]JSon) JSon
}

// ---------------------------------------------------------------------------------------------------------------------
// HttpClient definition
// ---------------------------------------------------------------------------------------------------------------------

type HttpClient struct {
    url string
    path string
}

func (this HttpClient) Handle(servicePath string, operation string, params, body, header map[string]JSon) JSon {
    return NewNull()
}