(ns swarmpit.component.menu
  (:require [material.component :as comp]
            [material.icon :as icon]
            [sablono.core :refer-macros [html]]
            [swarmpit.component.state :as state]
            [swarmpit.storage :as storage]
            [swarmpit.routes :as routes]
            [rum.core :as rum]))

(enable-console-print!)

(def cursor [:layout])

(def drawer-style
  {:boxShadow "none"})

(def drawer-container-style
  {:boxShadow   "none"
   :borderRight "1px solid #e0e4e7"
   :overflow    "hidden"})

(def drawer-container-closed-style
  (merge drawer-container-style
         {:width     "70px"
          :transform "translate(0px, 0px)"}))

(def drawer-container-opened-style
  (merge drawer-container-style
         {:width "200px"}))

(def drawer-icon-style
  {:padding    0
   :marginLeft 3})

(def logo
  (html
    [:img {:src    "img/swarmpit-transparent.png"
           :height "48"
           :width  "48"}]))

(def drawer-icon
  (comp/icon-button
    {:style drawer-icon-style}
    logo))

(def drawer-item-inner-style
  {:paddingLeft "50px"})

(def drawer-item-style
  {:paddingLeft "5px"
   :fontWeight  "lighter"
   :color       "rgb(117, 117, 117)"})

(def drawer-item-selected-style
  {:paddingLeft "5px"
   :fontWeight  "normal"
   :color       "#437f9d"})

(def drawer-category-style
  {:cursor "default"})

(def drawer-category-closed-style
  (merge drawer-category-style
         {:opacity 0}))

(def drawer-title-style
  {:lineHeight "normal"})

(def drawer-app-name-style
  {:marginTop  "13px"
   :fontSize   "20px"
   :fontWeight 200})

(def drawer-app-version-style
  {:fontSize   "11px"
   :fontWeight 300})

(def menu
  [{:name "APPLICATIONS"}
   {:name    "Services"
    :icon    icon/services
    :handler :service-list
    :domain  :service}
   {:name    "Tasks"
    :icon    icon/tasks
    :handler :task-list
    :domain  :task}
   {:name "INFRASTRUCTURE"}
   {:name    "Networks"
    :icon    icon/networks
    :handler :network-list
    :domain  :network}
   {:name    "Nodes"
    :icon    icon/nodes
    :handler :node-list
    :domain  :node}
   {:name "DATA"}
   {:name    "Volumes"
    :icon    icon/volumes
    :handler :volume-list
    :domain  :volume}
   {:name    "Secrets"
    :icon    icon/secrets
    :handler :secret-list
    :route   "secrets"
    :domain  :secret}
   {:name "DISTRIBUTION"}
   {:name    "Dockerhub"
    :icon    icon/docker
    :handler :dockerhub-user-list
    :domain  :dockerhub}
   {:name    "Registry"
    :icon    icon/registries
    :handler :registry-list
    :domain  :registry}])

(def admin-menu
  [{:name "ADMIN"}
   {:name    "Users"
    :icon    icon/users
    :handler :user-list
    :domain  :user}])

(def menu-style
  {:height   "100%"
   :overflow "auto"})

(rum/defc drawer-category < rum/static [name opened?]
  (let [drawer-category-style (if opened?
                                drawer-category-style
                                drawer-category-closed-style)]
    (comp/menu-item
      {:style       drawer-category-style
       :primaryText name
       :disabled    true})))

(rum/defc drawer-item < rum/static [name icon handler opened? selected?]
  (let [drawer-item-text (if opened?
                           name
                           nil)
        drawer-item-style (if selected?
                            drawer-item-selected-style
                            drawer-item-style)
        drawer-item-icon (if selected?
                           (comp/svg {:color "#437f9d"} icon)
                           (comp/svg icon))]
    (comp/menu-item
      {:style         drawer-item-style
       :innerDivStyle drawer-item-inner-style
       :primaryText   drawer-item-text
       :href          (routes/path-for-frontend handler)
       :leftIcon      drawer-item-icon})))

(rum/defc drawer-title < rum/static []
  (html [:div
         [:div {:style drawer-app-name-style} "swarmpit"]
         [:div {:style drawer-app-version-style} "1.2-SNAPSHOT"]]))

(rum/defc drawer < rum/reactive [page-domain]
  (let [{:keys [opened]} (state/react cursor)
        drawer-container-style (if opened
                                 drawer-container-opened-style
                                 drawer-container-closed-style)]
    (comp/mui
      (comp/drawer
        {:key            "menu-drawer"
         :open           opened
         :containerStyle drawer-container-style}
        (comp/app-bar
          {:key                      "menu-drawer-bar"
           :title                    (drawer-title)
           :titleStyle               drawer-title-style
           :style                    drawer-style
           :iconElementLeft          drawer-icon
           :onLeftIconButtonTouchTap (fn []
                                       (state/update-value [:opened] (not opened) cursor))})
        (comp/menu
          {:key   "menu"
           :style menu-style}
          (map
            (fn [menu-item]
              (let [icon (:icon menu-item)
                    name (:name menu-item)
                    handler (:handler menu-item)
                    domain (:domain menu-item)
                    selected (= page-domain domain)]
                (if (some? icon)
                  (drawer-item name icon handler opened selected)
                  (drawer-category name opened)))) (if (storage/admin?)
                                                     (concat menu admin-menu)
                                                     menu)))))))