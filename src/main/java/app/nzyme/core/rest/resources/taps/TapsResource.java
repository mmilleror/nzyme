package app.nzyme.core.rest.resources.taps;

import app.nzyme.core.NzymeNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import app.nzyme.plugin.rest.security.RESTSecured;
import app.nzyme.core.rest.responses.taps.metrics.TapMetricsGaugeHistogramResponse;
import app.nzyme.core.rest.responses.taps.metrics.TapMetricsGaugeHistogramValueResponse;
import app.nzyme.core.taps.metrics.BucketSize;
import app.nzyme.core.taps.metrics.TapMetrics;
import app.nzyme.core.taps.metrics.TapMetricsGauge;
import app.nzyme.core.rest.responses.taps.*;
import app.nzyme.core.rest.responses.taps.metrics.TapMetricsGaugeResponse;
import app.nzyme.core.rest.responses.taps.metrics.TapMetricsResponse;
import app.nzyme.core.taps.Bus;
import app.nzyme.core.taps.Capture;
import app.nzyme.core.taps.Channel;
import app.nzyme.core.taps.Tap;
import app.nzyme.core.taps.metrics.TapMetricsGaugeAggregation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Path("/api/taps")
@RESTSecured
@Produces(MediaType.APPLICATION_JSON)
public class TapsResource {

    private static final Logger LOG = LogManager.getLogger(TapsResource.class);

    @Inject
    private NzymeNode nzyme;

    @GET
    public Response findAll() {
        List<TapDetailsResponse> tapsResponse = Lists.newArrayList();
        Optional<List<Tap>> taps = nzyme.getTapManager().getTaps();
        if (taps.isPresent()) {
            for (Tap tap : taps.get()) {
                tapsResponse.add(buildTapResponse(tap));
            }
        }

        return Response.ok(TapListResponse.create(tapsResponse.size(), tapsResponse)).build();
    }

    @GET
    @Path("/show/{uuid}")
    public Response findTap(@PathParam("uuid") String tapId) {
        UUID uuid;
        try {
            uuid = UUID.fromString(tapId);
        } catch(IllegalArgumentException e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<Tap> tap = nzyme.getTapManager().findTap(uuid);

        if (tap.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.ok(buildTapResponse(tap.get())).build();
        }
    }

    @GET
    @Path("/show/{uuid}/metrics")
    public Response tapMetrics(@PathParam("uuid") String tapId) {
        UUID uuid;
        try {
            uuid = UUID.fromString(tapId);
        } catch(IllegalArgumentException e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<Tap> tap = nzyme.getTapManager().findTap(uuid);

        if (tap.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        TapMetrics metrics = nzyme.getTapManager().findMetricsOfTap(uuid);
        List<TapMetricsGauge> gauges = metrics.gauges();

        Map<String, TapMetricsGaugeResponse> parsedGauges = Maps.newHashMap();
        for (TapMetricsGauge gauge : gauges) {
            parsedGauges.put(
                    gauge.metricName(),
                    TapMetricsGaugeResponse.create(
                            gauge.metricName(),
                            gauge.metricValue(),
                            gauge.createdAt()
                    )
            );
        }

        return Response.ok(
                TapMetricsResponse.create(
                        parsedGauges
                )
        ).build();
    }

    @GET
    @Path("/show/{uuid}/metrics/gauges/{metricName}/histogram")
    public Response tapMetricsGauge(@PathParam("uuid") String tapId, @PathParam("metricName") String metricName) {
        UUID uuid;
        try {
            uuid = UUID.fromString(tapId);
        } catch(IllegalArgumentException e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<Tap> tap = nzyme.getTapManager().findTap(uuid);

        if (tap.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<Map<DateTime, TapMetricsGaugeAggregation>> histo = nzyme.getTapManager().findMetricsHistogram(
                uuid, metricName, 24, BucketSize.MINUTE
        );

        if (histo.isEmpty()) {
            return Response.ok(Maps.newHashMap()).build();
        }

        Map<DateTime, TapMetricsGaugeHistogramValueResponse> result = Maps.newTreeMap();
        for (TapMetricsGaugeAggregation value : histo.get().values()) {
            result.put(value.bucket(), TapMetricsGaugeHistogramValueResponse.create(
                    value.bucket(), value.average(), value.maximum(), value.minimum()
            ));
        }

        return Response.ok(TapMetricsGaugeHistogramResponse.create(result)).build();
    }
    
    private TapDetailsResponse buildTapResponse(Tap tap) {
        List<BusDetailsResponse> busesResponse = Lists.newArrayList();

        Optional<List<Bus>> buses = nzyme.getTapManager().findBusesOfTap(tap.uuid());
        if (buses.isPresent()) {
            for (Bus bus : buses.get()) {
                List<ChannelDetailsResponse> channelsResponse = Lists.newArrayList();

                Optional<List<Channel>> channels = nzyme.getTapManager().findChannelsOfBus(bus.id());
                if (channels.isPresent()) {
                    for (Channel channel : channels.get()) {
                        channelsResponse.add(ChannelDetailsResponse.create(
                                channel.name(),
                                channel.capacity(),
                                channel.watermark(),
                                TotalWithAverageResponse.create(
                                        channel.errors().total(),
                                        channel.errors().average()
                                ),
                                TotalWithAverageResponse.create(
                                        channel.throughputBytes().total(),
                                        channel.throughputBytes().average()
                                ),
                                TotalWithAverageResponse.create(
                                        channel.throughputMessages().total(),
                                        channel.throughputMessages().average()
                                )
                        ));
                    }
                }

                busesResponse.add(BusDetailsResponse.create(
                        bus.id(),
                        bus.name(),
                        channelsResponse
                ));
            }
        }

        List<CaptureDetailsResponse> capturesResponse = Lists.newArrayList();
        Optional<List<Capture>> captures = nzyme.getTapManager().findCapturesOfTap(tap.uuid());
        if (captures.isPresent()) {
            for (Capture capture : captures.get()) {
                capturesResponse.add(
                        CaptureDetailsResponse.create(
                                capture.interfaceName(),
                                capture.captureType(),
                                capture.isRunning(),
                                capture.received(),
                                capture.droppedBuffer(),
                                capture.droppedInterface(),
                                capture.updatedAt(),
                                capture.createdAt()
                        )
                );
            }
        }

        return TapDetailsResponse.create(
                tap.uuid(),
                tap.name(),
                tap.version(),
                tap.clock(),
                TotalWithAverageResponse.create(tap.processedBytes().total(), tap.processedBytes().average()),
                tap.memoryTotal(),
                tap.memoryFree(),
                tap.memoryUsed(),
                tap.cpuLoad(),
                tap.lastReport() == null ? false : tap.lastReport().isAfter(DateTime.now().minusMinutes(2)),
                tap.clockDriftMs(),
                tap.createdAt(),
                tap.updatedAt(),
                tap.lastReport(),
                tap.description(),
                busesResponse,
                capturesResponse
        );
    }

}
