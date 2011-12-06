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

import org.apache.jackrabbit.ocm.exception.JcrMappingException;
import org.apache.jackrabbit.ocm.mapper.impl.digester.DigesterDescriptorReader;
import org.apache.jackrabbit.ocm.mapper.model.MappingDescriptor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

/**
 * Factory bean for loading mapping files. This factory beans can load several file descriptors and assembles
 * them into an overall class descriptor.
 * @author Costin Leau
 */
public class MappingDescriptorFactoryBean implements FactoryBean<MappingDescriptor>, InitializingBean {

    //    private static final Log log = LogFactory.getLog(MappingDescriptorFactoryBean.class);

    private MappingDescriptor mappingDescriptor;

    private Resource[] mappings;

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    @Override
    public MappingDescriptor getObject() throws Exception {
        return mappingDescriptor;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    @Override
    public Class<MappingDescriptor> getObjectType() {
        //        return (this.mappingDescriptor != null) ? this.mappingDescriptor.getClass() : ClassDescriptor.class;
        return MappingDescriptor.class;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (mappings == null || mappings.length == 0)
            throw new IllegalArgumentException("at least one mapping file is needed");

        createMappingDescriptor();
    }

    /**
     * Subclasses can extend this method to provide custom behavior when creating the mapping descriptor
     */
    protected void createMappingDescriptor() throws IOException, JcrMappingException {
        // load the descriptors step by step and concatenate everything in an over-all
        // descriptor
        InputStream[] streams = new InputStream[mappings.length];
        for (int i = 0; i < streams.length; i++) {
            if (mappings[i] != null) {
                streams[i] = mappings[i].getInputStream();
            }
        }
        DigesterDescriptorReader reader = new DigesterDescriptorReader(streams);
        mappingDescriptor = reader.loadClassDescriptors();
    }

    /**
     * @return Returns the descriptors.
     */
    public Resource[] getMappings() {
        return mappings;
    }

    /**
     * @param descriptors The descriptors to set.
     */
    public void setMappings(Resource[] descriptors) {
        this.mappings = descriptors;
    }
}
