package app.nzyme.core.rest.responses.dot11.monitoring;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class MonitoredSSIDDetailsResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @JsonProperty("ssid")
    public abstract String ssid();

    @JsonProperty("organization_id")
    @Nullable
    public abstract UUID organizationId();

    @JsonProperty("tenant_id")
    @Nullable
    public abstract UUID tenantId();

    @JsonProperty("bssids")
    @Nullable
    public abstract List<MonitoredBSSIDDetailsResponse> bssids();

    @JsonProperty("channels")
    @Nullable
    public abstract List<MonitoredChannelResponse> channels();

    @JsonProperty("security_suites")
    @Nullable
    public abstract List<MonitoredSecuritySuiteResponse> securitySuites();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    @JsonProperty("updated_at")
    public abstract DateTime updatedAt();

    @JsonProperty("is_alerted")
    public abstract boolean isAlerted();

    public static MonitoredSSIDDetailsResponse create(UUID uuid, String ssid, UUID organizationId, UUID tenantId, List<MonitoredBSSIDDetailsResponse> bssids, List<MonitoredChannelResponse> channels, List<MonitoredSecuritySuiteResponse> securitySuites, DateTime createdAt, DateTime updatedAt, boolean isAlerted) {
        return builder()
                .uuid(uuid)
                .ssid(ssid)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .bssids(bssids)
                .channels(channels)
                .securitySuites(securitySuites)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .isAlerted(isAlerted)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MonitoredSSIDDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder ssid(String ssid);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder bssids(List<MonitoredBSSIDDetailsResponse> bssids);

        public abstract Builder channels(List<MonitoredChannelResponse> channels);

        public abstract Builder securitySuites(List<MonitoredSecuritySuiteResponse> securitySuites);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder isAlerted(boolean isAlerted);

        public abstract MonitoredSSIDDetailsResponse build();
    }
}
