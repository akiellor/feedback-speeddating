(ns ^:figwheel-always feedback-speeddating.core
  (:require
    [clojure.set :as s]
    [clojure.string :as string]
    [reagent.core :as reagent :refer [atom]]
    [cljs-time.core :as t]
    [cljs-time.format :as tf]))

(enable-console-print!)

(defonce app-state (atom {:text "Hello world!"}))

(defn sessions [names]
  (filter #(= (count %) 2) (set (apply concat (map (fn [i] (map #(set [i %]) names)) names)))))

(defn conflict? [slot session]
  (let [people-in-slot (set (apply concat slot))
        conflicts (s/intersection people-in-slot session)]
    (< 0 (count conflicts))))

(defn packer [timetable session]
  (let [available-slot (first (remove #(conflict? % session) timetable))]
    (cond
      available-slot (cons (cons session available-slot) (remove #{available-slot} timetable))
      :else (cons [session] timetable))))

(defn pack [sessions]
  (reduce packer [] sessions))

(defn tap [value]
  (println value)
  value)

(defn find-a-timetable [names start-at duration-minutes]
  (let [slots (pack (shuffle (sessions names)))]
    (map-indexed #(hash-map :start (t/plus start-at (t/minutes (* duration-minutes %1))) :slot %2) slots)))

(defn find-best-timetable [names start-at duration-minutes]
  (first (sort-by count (repeatedly 100 (partial find-a-timetable names start-at duration-minutes)))))

(defn d->session [session]
  ^{:key session}
  [:div.session
    [:span (str (first session))]
    [:span (str (last session))]])

(defn d->slot [slot]
  ^{:key slot}
    [:div.slot
     [:h2 (tf/unparse (tf/formatter "H:mm") (:start slot))]
     (map d->session (:slot slot))])

(defn d->timetable [app-state]
  (let [timetable (find-best-timetable
                    (:names @app-state)
                    (:start-at @app-state)
                    (:duration-minutes @app-state))]
    [:div.timetable (map d->slot timetable)]))

(defn d->names [app-state]
  [:input {:type "text" :on-change (fn [value] (swap! app-state assoc :names (break-space (-> value .-target .-value))))}])

(defn d->application [app-state]
  [:div
   [d->names app-state]
   [d->timetable app-state]])

(def app-state (atom {:names []
                      :start-at (t/today-at 15 30)
                      :duration-minutes 10}))

(defn break-space [v]
  (string/split v " "))

(defn main []
  (reagent/render-component [d->application app-state]
                            (.-body js/document)))
