(ns feedback-speeddating.dev
    (:require
     [feedback-speeddating.core :as c]
     [figwheel.client :as fw]))

(fw/start {
  :websocket-url "ws://localhost:3449/figwheel-ws"
  :on-jsload c/main})
