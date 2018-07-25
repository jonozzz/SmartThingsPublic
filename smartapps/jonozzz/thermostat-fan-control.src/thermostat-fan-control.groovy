/**
 *  REST Demo
 *
 *  Copyright 2016 Ionut Turturica
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Thermostat Fan Control",
    namespace: "jonozzz",
    author: "Ionut Turturica",
    description: "Routine triggered turn on/off heater in air circulation mode only and auto-turn off after X minutes.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    page(name: "configure")
}

def configure() {
    dynamicPage(name: "configure", title: "Configure Switch and Phrase", install: true, uninstall: true) {
        section("Thermostats") {
             input "thermostats", "capability.thermostat", multiple: true
        }

        section("Turn thermostat off automatically?") {
            input "turnOffTherm", "enum", metadata: [values: ["Yes", "No"]], required: false
        }

        section("Delayed turn off (defaults to 30 minutes)") {
            input "turnOffDelay", "decimal", title: "Number of minutes", required: false
        }

        def actions = location.helloHome?.getPhrases()*.label
        if (actions) {
            actions.sort()
            section("Actions") {
                log.trace actions
                input "onMode", "mode", title: "select a mode(s)", multiple: true, required: false
                input "offMode", "mode", title: "select a mode(s)", multiple: true, required: false
                input "onAction", "enum", title: "When to turn on the fan?", options: actions, multiple: true, required: true
                input "offAction", "enum", title: "When to turn off the fan?", options: actions, multiple: true, required: false
            }
        }
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize() {
    subscribe(location, "routineExecuted", routineChanged)
    subscribe(location, "mode", modeChangeHandler)
    log.debug "selected on action $onAction"
    log.debug "selected off action $offAction"
}

def routineChanged(evt) {
	if (onAction.contains(evt.displayName)) {
    	thermoTurnOn()
        thermoShutOffTrigger()
    }

	if (evt.displayName == settings.offAction) {
    	thermoShutOff()
    }
    /*
    log.debug "routineChanged: $evt"
    log.debug "action selected: ${settings.onAction}"
    log.debug "evt name: ${evt.name}"
    log.debug "evt value: ${evt.value}"
    log.debug "evt displayName: ${evt.displayName}"
    log.debug "evt descriptionText: ${evt.descriptionText}"*/
}

def modeChangeHandler(evt) {
    log.debug "mode changed to ${evt.value}"
	if (onMode && onMode.contains(evt.value)) {
    	thermoTurnOn()
        thermoShutOffTrigger()
    }

	if (offMode && offMode.contains(evt.value)) {
    	thermoShutOff()
    }

}

def thermoShutOffTrigger() {
  if(turnOffTherm == "Yes") {
    log.info("Starting timer to turn off thermostat")
    def delay = (turnOffDelay != null && turnOffDelay != "") ? turnOffDelay * 60 : 30 * 60
    state.turnOffTime = now()

    sendNotificationEvent("I turned on the fan and will be turning it off in ${delay/60} minutes.")
    //sendPush("I turned on the fan and will be turning it off in ${delay/60} minutes.")
	
    //unschedule()
    runIn(delay, "thermoShutOff")
  }
}

def thermoTurnOn() {
    //log.warn "turning on the fan"
    //sendNotificationEvent("The fan is turning on.")
    thermostats?.setThermostatFanMode("on")
    //thermostats?.setFanMode("on")
    //thermostats?.thermostatFanMode("on")
    //thermostats?.fanOn()
}

def thermoShutOff() {
    //log.warn "turning off the fan"
    sendNotificationEvent("The fan is turning off.")
	thermostats?.setThermostatFanMode("auto")
    //thermostats?.setFanMode("auto")
    //thermostats?.thermostatFanMode("auto")
    //thermostats?.fanAuto()
}
