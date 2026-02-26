#!/bin/sh

# Deployment
echo "Deploying Image $TAG_IMAGE_NAME"

cat <<EOF > deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: $APP_NAME-deployment
  labels:
    app: $APP_NAME
spec:
  replicas: 1
  strategy:
    type: Recreate
  selector:
    matchLabels:
      app: $APP_NAME
  template:
    metadata:
      labels:
        app: $APP_NAME
    spec:
      containers:
      - name: $APP_NAME
        image: $TAG_IMAGE_NAME 
        ports:
        - containerPort: $PORT
        env:
          - name: HTTP_PROXY
            value: "$HTTP_PROXY"
          - name: HTTPS_PROXY
            value: "$HTTP_PROXY"
          - name: NO_PROXY
            value: "localhost,127.0.0.1,.cluster.local,.svc"
          - name: JWT_SECRET_KEY
            value: "${PODS_JWT_SECRET_KEY}"
          - name: SPRING_PROFILES_ACTIVE
            value: "prod"
          - name: DB_HOST
            value: "${PODS_DB_HOST}"
          - name: DB_PORT
            value: "${PODS_DB_PORT}"
          - name: DB_NAME
            value: "${PODS_DB_NAME}"
          - name: DB_USERNAME
            value: "${PODS_DB_USERNAME}"
          - name: DB_PASSWORD
            value: "${PODS_DB_PASSWORD}"
        resources:
          requests:
            memory: "$MIN_MEMORY" 
            cpu: "$MIN_CPU"
          limits:
            memory: "$MAX_MEMORY"
            cpu: "$MAX_CPU"
      imagePullSecrets:
      - name: pkpl-registry
EOF

yq e '.spec.template.spec.containers[0].env += load("env.yaml")' -i deployment.yaml

kubectl apply -f deployment.yaml --kubeconfig ./kubeconfig

echo "Deployment done"

# Service
echo "Creating Service $APP_NAME"

cat <<EOF > service.yaml
apiVersion: v1
kind: Service
metadata:
  name: $APP_NAME
  labels:
    app: $APP_NAME
spec:
  type: ClusterIP
  selector:
    app: $APP_NAME
  ports:
    - protocol: TCP
      port: $PORT
      targetPort: $PORT
EOF

kubectl apply -f service.yaml --kubeconfig ./kubeconfig

echo "Service creation done"

# Ingress
echo "Creating Ingress $APP_NAME"

INGRESS_HOST=${INGRESS_HOST#pkpl-}
cat <<EOF > ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: $APP_NAME
  annotations:
    kubernetes.io/ingress.class: "nginx"
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  ingressClassName: nginx
  rules:
  - host: $INGRESS_HOST.pkpl.cs.ui.ac.id
    http:
      paths:
      - backend:
          service:
            name: $APP_NAME
            port:
              number: $PORT
        path: /
        pathType: Prefix
EOF

kubectl apply -f ingress.yaml --kubeconfig ./kubeconfig

echo "Ingress creation done"
