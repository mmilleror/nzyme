import React, {useContext, useEffect, useRef, useState} from "react";
import Store from "../../util/Store";
import TapsService from "../../services/TapsService";
import {TapContext} from "../../App";

const tapsService = new TapsService();

function TapSelector(props) {

  const tapContext = useContext(TapContext);

  const selectedTaps = tapContext.taps;
  const setSelectedTaps = tapContext.set;
  const [show, setShow] = useState(false);

  const [availableTaps, setAvailableTaps] = useState(null);
  const [availableTapsUUIDs, setAvailableTapsUUIDs] = useState(null);
  const [preSelectedTaps, setPreSelectedTaps] = useState(null);

  const [hasSelectedOfflineTap, setHasSelectedOfflineTap] = useState(false);

  const [buttonText, setButtonText] = useState(null);

  useEffect(() => {
    setAvailableTaps(null);

    tapsService.findAllTapsHighLevel(function (response) {
      setAvailableTaps(response.data.taps);

      let lsTaps = Store.get("selected_taps");
      if (lsTaps === undefined || lsTaps === null || !Array.isArray(lsTaps)) {
        setSelectedTaps("*");
        setPreSelectedTaps("*");
        setButtonText("All Taps Selected");
      } else {
        setSelectedTaps(lsTaps);
        setPreSelectedTaps(lsTaps);

        if (lsTaps.length > 1) {
          setButtonText(lsTaps.length + " Taps Selected");
        } else {
          setButtonText("1 Tap Selected");
        }
      }
    });
  }, [])

  useEffect(() => {
    setHasSelectedOfflineTap(false); // Reset.

    if (selectedTaps !== null && availableTaps !== null) {
      if (availableTaps.length === 0) {
        setButtonText("No access to any taps.");
      } else {
        if (selectedTaps === "*") {
          setButtonText("All Taps Selected");

          // Is any tap currently offline?
          availableTaps.forEach(function (availableTap) {
            if (!availableTap.is_online) {
              setHasSelectedOfflineTap(true);
            }
          });
        } else {
          /*
           * Reset everything if a tap is no longer available (permissions may have changed or local
           * storage came from other session)
           */
          if (availableTapsUUIDs !== null) {
            let invalidTapFound = false;
            selectedTaps.forEach(function (selectedTap) {
              if (!availableTapsUUIDs.includes(selectedTap)) {
                invalidTapFound = true;
              }
            });

            if (invalidTapFound) {
              Store.set("selected_taps", "*");
              setSelectedTaps("*");
              setPreSelectedTaps("*");
            }

            // Is any of the selected taps currently offline?
            selectedTaps.forEach(function (selectedTap) {
              availableTaps.forEach(function (availableTap) {
                if (availableTap.uuid === selectedTap && !availableTap.is_online) {
                  setHasSelectedOfflineTap(true);
                }
              });
            });
          }

          if (selectedTaps && selectedTaps.length > 1) {
            setButtonText(selectedTaps.length + " Taps Selected");
          } else {
            setButtonText("1 Tap Selected");
          }
        }
      }
    }
  }, [selectedTaps, availableTaps])

  useEffect(() => {
    if (availableTaps !== null) {
      const uuids = [];

      availableTaps.forEach(function (availableTap) {
        uuids.push(availableTap.uuid);
      });

      setAvailableTapsUUIDs(uuids);
    }
  }, [availableTaps]);

  const toggleMenu = function() {
    setShow(!show);
  }

  const handleTapSelection = function(e, uuid) {
    e.preventDefault();

    const taps = preSelectedTaps === "*" ? [] : [...preSelectedTaps];
    if (taps.includes(uuid)) {
      // Remove a tap.
      if (taps.length > 1) {
        const idx = taps.indexOf(uuid);
        taps.splice(idx, 1);
        setPreSelectedTaps(taps);
      } else {
        // Removed last tap.
        setPreSelectedTaps("*");
      }
    } else {
      // Add a new tap.
      taps.push(uuid);
      setPreSelectedTaps(taps);
    }
  }

  const onSelectTaps = function(e) {
    e.preventDefault();

    Store.set("selected_taps", preSelectedTaps);
    setSelectedTaps(preSelectedTaps);

    setShow(false);
  }

  const selectAllTaps = function(e) {
    e.preventDefault();

    setPreSelectedTaps("*");
  }

  if (availableTaps === null || selectedTaps === null || preSelectedTaps === null) {
    return (
        <div className="dropdown">
          <button className="btn btn-outline-secondary dropdown-toggle" type="button" disabled={true}>
            <i className="fa-solid fa-spinner fa-spin-pulse fa-sm"></i> Tap Selector Loading
          </button>
        </div>
    )
  }

  return (
    <React.Fragment>
      <div className="dropdown">
        <button className="btn btn-outline-secondary dropdown-toggle"
                type="button"
                aria-expanded="false"
                data-bs-auto-close="false"
                onClick={toggleMenu}
                disabled={availableTaps.length === 0}>
          {buttonText}{' '}
          {hasSelectedOfflineTap ? <i className="fa-solid fa-triangle-exclamation text-warning"></i> : null }
        </button>
        <ul className="dropdown-menu" style={{display: show ? "block" : "none"}}>
          <li>
            <a className={"dropdown-item " + (preSelectedTaps === "*" ? "active" : null)} href="#" onClick={selectAllTaps}>
              All Taps
            </a>
          </li>
          <li><hr className="dropdown-divider" /></li>
          {availableTaps.map(function(tap, i) {
            return (
                <li key={"tapselector-tap-" + i}>
                  <a className={"dropdown-item " + (preSelectedTaps.includes(tap.uuid) ? "active" : null)}
                     href="#"
                     onClick={(e) => handleTapSelection(e, tap.uuid)}>
                    {tap.name}

                    {!tap.is_online ? <span className="text-warning"> (Offline)</span> : null }
                  </a>
                </li>
            )
          })}
          <li className="tap-selector-actions">
            <button className="btn btn-primary btn-sm tap-selector-select" onClick={onSelectTaps}>
              Select Taps
            </button>
          </li>
        </ul>
      </div>
    </React.Fragment>
  )

}

export default TapSelector;