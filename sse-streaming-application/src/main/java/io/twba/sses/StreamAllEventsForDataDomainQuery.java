package io.twba.sses;

import io.twba.sses.shared.Query;

public record StreamAllEventsForDataDomainQuery(String dataDomain, String consumerId) implements Query {

    DataDomain getDataDomain() {
        return new DataDomain(dataDomain);
    }

}
