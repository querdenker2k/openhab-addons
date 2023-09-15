/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.senechome.internal.json;

import java.io.Serializable;

/**
 * Json model of mein-senec.de dashboard api, rebuilt by analyzing the api.
 *
 * @author Korbinian Probst - Initial Contribution
 */
public class MeinSenecResponse implements Serializable {
    private Aktuell aktuell;
    private Heute heute;
    private String zeitstempel;
    private boolean electricVehicleConnected;

    public Aktuell getAktuell() {
        return aktuell;
    }

    public Heute getHeute() {
        return heute;
    }

    public String getZeitstempel() {
        return zeitstempel;
    }

    public boolean isElectricVehicleConnected() {
        return electricVehicleConnected;
    }

    public static class Aktuell implements Serializable {
        private Wert stromerzeugung;
        private Wert stromverbrauch;
        private Wert netzeinspeisung;
        private Wert netzbezug;
        private Wert speicherbeladung;
        private Wert speicherentnahme;
        private Wert speicherfuellstand;
        private Wert autarkie;
        private Wert wallbox;

        public Wert getStromerzeugung() {
            return stromerzeugung;
        }

        public Wert getStromverbrauch() {
            return stromverbrauch;
        }

        public Wert getNetzeinspeisung() {
            return netzeinspeisung;
        }

        public Wert getNetzbezug() {
            return netzbezug;
        }

        public Wert getSpeicherbeladung() {
            return speicherbeladung;
        }

        public Wert getSpeicherentnahme() {
            return speicherentnahme;
        }

        public Wert getSpeicherfuellstand() {
            return speicherfuellstand;
        }

        public Wert getAutarkie() {
            return autarkie;
        }

        public Wert getWallbox() {
            return wallbox;
        }
    }

    public static class Heute implements Serializable {
        private Wert stromerzeugung;
        private Wert stromverbrauch;
        private Wert netzeinspeisung;
        private Wert netzbezug;
        private Wert speicherbeladung;
        private Wert speicherentnahme;
        private Wert speicherfuellstand;
        private Wert autarkie;
        private Wert wallbox;

        public Wert getStromerzeugung() {
            return stromerzeugung;
        }

        public Wert getStromverbrauch() {
            return stromverbrauch;
        }

        public Wert getNetzeinspeisung() {
            return netzeinspeisung;
        }

        public Wert getNetzbezug() {
            return netzbezug;
        }

        public Wert getSpeicherbeladung() {
            return speicherbeladung;
        }

        public Wert getSpeicherentnahme() {
            return speicherentnahme;
        }

        public Wert getSpeicherfuellstand() {
            return speicherfuellstand;
        }

        public Wert getAutarkie() {
            return autarkie;
        }

        public Wert getWallbox() {
            return wallbox;
        }
    }

    public static class Wert implements Serializable {
        private double wert;
        private String einheit;

        public double getWert() {
            return wert;
        }

        public String getEinheit() {
            return einheit;
        }
    }
}
