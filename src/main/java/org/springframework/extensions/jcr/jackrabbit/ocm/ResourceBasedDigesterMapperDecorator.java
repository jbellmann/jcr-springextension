package org.springframework.extensions.jcr.jackrabbit.ocm;

import org.apache.jackrabbit.ocm.mapper.Mapper;
import org.apache.jackrabbit.ocm.mapper.impl.digester.DigesterMapperImpl;
import org.apache.jackrabbit.ocm.mapper.model.ClassDescriptor;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * User: dsklyut Date: Dec 8, 2009 Time: 9:33:38 AM
 */
public class ResourceBasedDigesterMapperDecorator implements Mapper {

    private DigesterMapperImpl decorated;

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

    public ClassDescriptor getClassDescriptorByClass(Class clazz) {
        return this.decorated.getClassDescriptorByClass(clazz);
    }

    public ClassDescriptor getClassDescriptorByNodeType(String jcrNodeType) {
        return this.decorated.getClassDescriptorByNodeType(jcrNodeType);
    }
}
