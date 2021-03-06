(ns swarmpit.component.service.form-secrets
  (:require [material.component :as comp]
            [material.icon :as icon]
            [swarmpit.routes :as routes]
            [swarmpit.component.handler :as handler]
            [swarmpit.component.state :as state]
            [rum.core :as rum]))

(enable-console-print!)

(def cursor [:page :service :wizard :secrets])

(defonce secrets (atom []))

(defn secrets-handler
  []
  (handler/get
    (routes/path-for-backend :secrets)
    {:on-success (fn [response]
                   (reset! secrets response))}))

(def headers [{:name  "Name"
               :width "300px"}])

(def empty-info
  (comp/form-value "No secrets defined for the service."))

(def undefined-info
  (comp/form-icon-value
    icon/info
    [:span "No secrets found. Create new "
     [:a {:href (routes/path-for-frontend :secret-create)} "secret."]]))

(defn- form-secret [value index secrets-list]
  (comp/form-list-selectfield
    {:name     (str "form-secret-select-" index)
     :key      (str "form-secret-select-" index)
     :value    value
     :onChange (fn [_ _ v]
                 (state/update-item index :secretName v cursor))}
    (->> secrets-list
         (map #(comp/menu-item
                 {:name        (str "form-secret-item-" (:secretName %))
                  :key         (str "form-secret-item-" (:secretName %))
                  :value       (:secretName %)
                  :primaryText (:secretName %)})))))

(defn- render-secrets
  [item index data]
  (let [{:keys [secretName]} item]
    [(form-secret secretName index data)]))

(defn- form-table
  [secrets secrets-list]
  (comp/form-table-headless headers
                            secrets
                            secrets-list
                            render-secrets
                            (fn [index] (state/remove-item index cursor))))

(defn- add-item
  []
  (state/add-item {:secretName ""} cursor))

(rum/defc form-create < rum/reactive []
  (let [secrets-list (rum/react secrets)
        secrets (state/react cursor)]
    [:div
     (if (empty? secrets-list)
       undefined-info
       (comp/form-add-btn "Expose secrets" add-item))
     (when (not (empty? secrets))
       (form-table secrets secrets-list))]))

(rum/defc form-update < rum/reactive []
  (let [secrets-list (rum/react secrets)
        secrets (state/react cursor)]
    (if (empty? secrets)
      empty-info
      (form-table secrets secrets-list))))