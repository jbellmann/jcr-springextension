/**
 * Copyright 2009-2012 the original author or authors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.springframework.extensions.jcr.jackrabbit.ocm;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.jackrabbit.ocm.mapper.Mapper;
import org.apache.jackrabbit.ocm.mapper.impl.digester.DigesterMapperImpl;
import org.apache.jackrabbit.ocm.mapper.model.ClassDescriptor;
import org.springframework.core.io.Resource;

/**
 * User: dsklyut Date: Dec 8, 2009 Time: 9:33:38 AM
 */
public class ResourceBasedDigesterMapperDecorator implements Mapper {

    private final DigesterMapperImpl decorated;

    public ResourceBasedDigesterMapperDecorator(Resource resource) throws IOException {
        decorated = new DigesterMapperImpl(resource.getInputStream());
    }

    public ResourceBasedDigesterMapperDecorator(Resource[] resources) throws IOException {
        decorated = new DigesterMapperImpl(toInputStream(resources));
    }

    private InputStream[] toInputStream(Resource[] resources) throws IOException {
        List<InputStream> result = new ArrayList<InputStream>();
        for (Resource r : resources) {
            result.add(r.getInputStream());
        }
        return result.toArray(new InputStream[result.size()]);
    }

    @Override
    public ClassDescriptor getClassDescriptorByClass(Class clazz) {
        return this.decorated.getClassDescriptorByClass(clazz);
    }

    @Override
    public ClassDescriptor getClassDescriptorByNodeType(String jcrNodeType) {
        return this.decorated.getClassDescriptorByNodeType(jcrNodeType);
    }
}
