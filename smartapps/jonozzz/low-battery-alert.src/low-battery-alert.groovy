/**
 *  Low Battery Alert
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
    name: "Low Battery Alert",
    namespace: "jonozzz",
    author: "Ionut Turturica",
    description: "This app will poll selected devices that use a battery and send an alert when the level reaches a specified threshold.",
    category: "Convenience",
    iconUrl: "http://www.stevemeyers.net/wp-content/uploads/2015/02/battery-60px.png",
    iconX2Url: "http://www.stevemeyers.net/wp-content/uploads/2015/02/battery-120px.png")


preferences {
    section("About") {
        paragraph "This app will poll selected devices that use a battery and send an alert when the level reaches a specified threshold."
        paragraph "You may configure up to four groups with different thresholds."
    }
    for (int i = 1; i <= 4; i++) {
        section("Monitoring group ${i}") {
            input "group_${i}", "capability.battery", title: "Select devices to monitor", multiple: true, required: false
            input "threshold_${i}", "number", title: "Notify if battery is below", defaultValue: 25
        }
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unschedule()
    //unsubscribe()
	initialize()
}

def initialize() {
    //Second Minute Hour DayOfMonth Month DayOfWeek Year
    schedule("12 30 0 * * ?", check_batteries)
    check_batteries()
}

// TODO: implement event handlers
def check_batteries() {
    def size, batteries, device, threshold, value;

    for (int i = 1; i <= 4; i++) {
        size = settings["group_${i}"]?.size() ?: 0
        if (size > 0) {
            threshold = settings."threshold_${i}".toInteger()
            log.debug "Checking batteries for group ${i} (threshold ${threshold})"
            
            batteries = settings."group_${i}".currentValue("battery")
            for (int j = 0; j < size; j++) {
                  device = settings["group_${i}"][j]
                if (device != null) {
                    value = batteries[j]
                    if (value < threshold) {
                        log.debug "The $device battery is at ${value}, below threshold (${threshold})"
                        sendPush("The $device battery is at ${value}, below threshold (${threshold})")
                    } else {
                        log.debug "The $device battery is at ${value}"
                    }
                }
            }
        } else {
            log.debug "Group ${i} has no devices (${size})"
        }
    } 
}
