import React, {useEffect, useState} from "react";
import {Navigate, useParams} from "react-router-dom";
import LoadingSpinner from "../../misc/LoadingSpinner";
import Dot11Service from "../../../services/Dot11Service";
import ApiRoutes from "../../../util/ApiRoutes";
import moment from "moment";
import Dot11MonitoredBSSIDs from "./Dot11MonitoredBSSIDs";
import {notify} from "react-notify-toast";
import RefreshGears from "../../misc/RefreshGears";
import MonitoredChannelsTable from "./MonitoredChannelsTable";
import MonitoredSecuritySuitesTable from "./MonitoredSecuritySuitesTable";

const dot11Service = new Dot11Service();
const MAC_ADDRESS_REGEX = /^[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}$/;

function Dot11MonitoredNetworkDetailsPage() {

  const {uuid} = useParams();

  const [ssid, setSSID] = useState(null);
  const [revision, setRevision] = useState(0);

  const [newBSSID, setNewBSSID] = useState("");
  const [bssidFormSubmitting, setBSSIDFormSubmitting] = useState(false);

  const [newChannel, setNewChannel] = useState("");
  const [channelFormSubmitting, setChannelFormSubmitting] = useState(false);
  const [existingChannels, setExistingChannels] = useState([]);

  const [newSecuritySuite, setNewSecuritySuite] = useState("");
  const [securitySuiteFormSubmitting, setSecuritySuiteFormSubmitting] = useState(false);
  const [existingSecuritySuites, setExistingSecuritySuites] = useState([]);

  const [isLoading, setIsLoading] = useState(false);

  const [deleted, setDeleted] = useState(false);

  const onDelete = function () {
    if (!confirm("Really delete monitored network and all it's configuration?")) {
      return;
    }

    dot11Service.deleteMonitoredSSID(ssid.uuid, function () {
      notify.show("Monitored network deleted.", "success");
      setDeleted(true);
    });
  }

  const addBSSID = function (bssid) {
    setBSSIDFormSubmitting(true);
    dot11Service.createMonitoredBSSID(ssid.uuid, bssid, function () {
      bumpRevision();
      notify.show("Monitored BSSID added.", "success");
      setNewBSSID("");
      setBSSIDFormSubmitting(false);
    }, function () {
      notify.show("Could not add monitored BSSID. Please check nzyme log file.", "error");
      setBSSIDFormSubmitting(false);
    })
  }

  const addChannel = function (channel) {
    setChannelFormSubmitting(true);
    dot11Service.createMonitoredChannel(ssid.uuid, channel, function () {
      bumpRevision();
      notify.show("Monitored channel added.", "success");
      setNewChannel("");
      setChannelFormSubmitting(false);
    }, function () {
      notify.show("Could not add monitored channel. Please check nzyme log file.", "error");
      setChannelFormSubmitting(false);
    })
  }

  const addSecuritySuite = function (suite) {
    setSecuritySuiteFormSubmitting(true);
    dot11Service.createMonitoredSecuritySuite(ssid.uuid, suite, function () {
      bumpRevision();
      notify.show("Monitored security suite added.", "success");
      setNewSecuritySuite("");
      setSecuritySuiteFormSubmitting(false);
    }, function () {
      notify.show("Could not add monitored security suite. Please check nzyme log file.", "error");
      setSecuritySuiteFormSubmitting(false);
    })
  }

  const bumpRevision = function () {
    setRevision(revision+1);
  }

  const addBSSIDFormEnabled = function () {
    return MAC_ADDRESS_REGEX.test(newBSSID);
  }

  const addChannelFormEnabled = function () {
    return parseInt(newChannel, 10) > 0 && !existingChannels.includes(parseInt(newChannel, 10));
  }

  const addSecuritySuiteFormEnabled = function () {
    return newSecuritySuite.includes("-")
        && newSecuritySuite.includes("/")
        && !existingSecuritySuites.includes(newSecuritySuite);
  }

  // More complex loading/refreshing logic because of modals that may be open on the page.
  useEffect(() => {
    setIsLoading(true);
    dot11Service.findMonitoredSSID(uuid, setSSID, function() {
      setIsLoading(false);
    });
  }, [uuid, revision]);

  useEffect(() => {
    if (ssid) {
      let ec = [];
      ssid.channels.map(function (channel, i) {
        ec.push(channel.frequency);
      });

      let ess = [];
      ssid.security_suites.map(function (suite, i) {
        ess.push(suite.suite);
      });

      setExistingSecuritySuites(ess);
      setExistingChannels(ec);
    }
  }, [ssid])

  if (deleted) {
    return <Navigate to={ApiRoutes.DOT11.MONITORING.INDEX} />
  }

  if (!ssid) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <div className="row">
          <div className="col-md-8">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.OVERVIEW}>WiFi</a></li>
                <li className="breadcrumb-item"><a href={ApiRoutes.DOT11.MONITORING.INDEX}>Monitoring</a></li>
                <li className="breadcrumb-item">Monitored Networks</li>
                <li className="breadcrumb-item active" aria-current="page">{ssid.ssid}</li>
              </ol>
            </nav>
          </div>

          <div className="col-md-4">
            <span className="float-end">
              <a className="btn btn-secondary" href={ApiRoutes.DOT11.MONITORING.CONFIGURATION_IMPORT(ssid.uuid)}>
                Import Configuration
              </a>{' '}
              <a className="btn btn-danger" href="#" onClick={onDelete}>Delete</a>{' '}
              <a className="btn btn-primary" href={ApiRoutes.DOT11.MONITORING.INDEX}>Back</a>
            </span>
          </div>

          <div className="col-md-12">
            <h1>
              Monitored Network &quot;{ssid.ssid}&quot; {isLoading ? <RefreshGears /> : null}
            </h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Metadata</h3>

                <dl className="mb-0">
                  <dt>Configuration created at</dt>
                  <dd title={moment(ssid.creeated_at).format()}>{moment(ssid.created_at).fromNow()}</dd>
                  <dt>Configuration last changed at</dt>
                  <dd title={moment(ssid.updated_at).format()}>{moment(ssid.updated_at).fromNow()}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <h3>Monitored BSSIDs / Access Points of Network {isLoading ? <RefreshGears /> : null}</h3>

                <Dot11MonitoredBSSIDs bssids={ssid.bssids} bumpRevision={bumpRevision} parentIsLoading={isLoading} />

                <div className="input-group mb-3">
                  <input type="text"
                         className="form-control"
                         placeholder="18:7C:0B:D6:EC:F8"
                         value={newBSSID}
                         onChange={(e) => setNewBSSID(e.target.value)} />
                  <button className="btn btn-secondary"
                          disabled={!addBSSIDFormEnabled()}
                          onClick={() => { addBSSID(newBSSID) }}>
                    {bssidFormSubmitting ? "Please wait..." : "Add BSSID"}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Monitored Channels {isLoading ? <RefreshGears /> : null}</h3>

                <MonitoredChannelsTable ssid={ssid} bumpRevision={bumpRevision} />

                <div className="input-group mb-3">
                  <input type="number"
                         className="form-control"
                         placeholder="2462"
                         value={newChannel}
                         onChange={(e) => setNewChannel(e.target.value)} />
                  <span className="input-group-text">MHz</span>
                  <button className="btn btn-secondary"
                          disabled={!addChannelFormEnabled()}
                          onClick={() => { addChannel(newChannel) }}>
                    {channelFormSubmitting ? "Please wait..." : "Add Frequency "}
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div className="col-md-6">
            <div className="card">
              <div className="card-body">
                <h3>Monitored Security Suites {isLoading ? <RefreshGears /> : null}</h3>

                <MonitoredSecuritySuitesTable ssid={ssid} bumpRevision={bumpRevision} />

                <div className="input-group mb-3">
                  <input type="text"
                         className="form-control"
                         placeholder="CCMP-CCMP/PSK"
                         value={newSecuritySuite}
                         onChange={(e) => setNewSecuritySuite(e.target.value)} />
                  <button className="btn btn-secondary"
                          disabled={!addSecuritySuiteFormEnabled()}
                          onClick={() => { addSecuritySuite(newSecuritySuite) }}>
                    {securitySuiteFormSubmitting ? "Please wait..." : "Add Security Suite "}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
  )

}

export default Dot11MonitoredNetworkDetailsPage;