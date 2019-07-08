/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.search.aggregations.support;

import org.elasticsearch.search.aggregations.Aggregator;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.AggregatorFactory;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;
import org.elasticsearch.search.internal.SearchContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class ValuesSourceAggregatorFactory<VS extends ValuesSource, AF extends ValuesSourceAggregatorFactory<VS, AF>>
        extends AggregatorFactory<AF> {

    protected ValuesSourceConfig<VS> config;

    public ValuesSourceAggregatorFactory(String name, ValuesSourceConfig<VS> config, SearchContext context,
            AggregatorFactory<?> parent, AggregatorFactories.Builder subFactoriesBuilder, Map<String, Object> metaData) throws IOException {
        super(name, context, parent, subFactoriesBuilder, metaData);
        this.config = config;
    }

    @Override
    public Aggregator createInternal(Aggregator parent, boolean collectsFromSingleBucket,
            List<PipelineAggregator> pipelineAggregators, Map<String, Object> metaData) throws IOException {
        VS vs = config.toValuesSource(context.getQueryShardContext(), this::resolveMissingAny);
        if (vs == null) {
            return createUnmapped(parent, pipelineAggregators, metaData);
        }
        return doCreateInternal(vs, parent, collectsFromSingleBucket, pipelineAggregators, metaData);
    }

    /**
     * This method provides a hook for aggregations that need finer grained control over the ValuesSource selected when the user supplies a
     * missing value and there is no mapped field to infer the type from.  This will only be called for aggregations that specify the
     * ValuesSourceType.ANY in their constructors (On the builder class).  The user supplied object is passed as a parameter, so its type
     * may be inspected as needed.
     *
     * Generally, only the type of the returned ValuesSource is used, so returning the EMPTY instance of the chosen type is recommended.
     *
     * @param missing The user supplied missing value
     * @return A ValuesSource instance compatible with the supplied parameter
     */
    protected ValuesSource resolveMissingAny(Object missing) {
        return ValuesSource.Bytes.WithOrdinals.EMPTY;
    }

    protected abstract Aggregator createUnmapped(Aggregator parent,
            List<PipelineAggregator> pipelineAggregators, Map<String, Object> metaData) throws IOException;

    protected abstract Aggregator doCreateInternal(VS valuesSource, Aggregator parent,
            boolean collectsFromSingleBucket, List<PipelineAggregator> pipelineAggregators, Map<String, Object> metaData)
            throws IOException;

}
