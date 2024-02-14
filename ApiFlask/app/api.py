import time
from flask import (
    Blueprint,
    request,
    jsonify
)
from firebase_admin import  db
import json
import pandas as pd
import os
from firebase_admin import messaging

bp = Blueprint('api',__name__, url_prefix="/")

ref = db.reference('/Users')
@bp.route('/upload', methods=['POST'])
def upload_file():
    userId = request.form['user_id']
    if request.files['excel']:
        # Lee el archivo Excel y carga los datos en un DataFrame
        df = pd.read_excel(request.files['excel'])
        # Filtra y selecciona las columnas deseadas
        selected_columns = ['nombre', 'descripcion', 'precio', 'cantidad']
        filtered_df = df[selected_columns]
        
        # Convierte el DataFrame a formato JSON
        json_data = filtered_df.to_json(orient='records')
        products = json.loads(json_data)
        data = {}
        for i, product in enumerate(products):
            product_id = f"{i}"
            product["id"] = product_id
            data[product_id] = product

        final_data = {
            "Products": data
        }
        print(final_data)
        return final_data


@bp.route('/save', methods=['POST'])
def save_file():
    error = ""
    userId = request.form['user_id']
    products = request.form['json']
    products = json.loads(products)
    data = {}
    for i, product in enumerate(products):
        product_id = f"{i}"
        product["id"] = product_id
        data[product_id] = product

    final_data = {
        "Products": data
    }
    ref.child(userId).update(final_data)
    error = "ok"
    return error
    
@bp.route('/products/<userId>', methods=['POST'])
def get_products(userId):
    userId = request.form['user_id']
    # Obtiene los datos del nodo "Products"
    products = ref.child(userId).child('Products').get()
    return products

@bp.route('/sales/<userId>', methods=['POST'])
def sales(userId):
    error = ""
    userId = request.form['user_id']
    sales = request.form['json']
    sales = json.loads(sales)
    products = get_products(userId)

    # # Itera sobre todos los nodos en el nivel superior del diccionario
    # for node, items in sales.items():
    # # Itera sobre las claves y valores en el diccionario anidado
    #     for key, value in items.items():
    #         # Si la clave es un número (lo que indica que es un producto)
    #         if key.isdigit():
    #             # Imprime la cantidad de ese producto
    #             print("La cantidad del producto", key, "en el nodo", node, "es:", value['cantidad'])

    # Itera sobre todos los nodos en el nivel superior del diccionario de ventas
    for node, items in sales.items():
    # Itera sobre las claves y valores en el diccionario anidado de ventas
        for key, sale in items.items():
        # Si la clave es un número (lo que indica que es un producto)
            if key.isdigit():
                # Obtiene el ID del producto en ventas
                sale_id = sale['id']
                # Recorre todos los productos
                for product in products:
                    # Si el ID del producto en ventas coincide con el ID del producto en productos
                    if product['id'] == sale_id:
                        # Resta la cantidad de ventas de la cantidad de productos
                        product['cantidad'] -= sale['cantidad']
                        ref.child(userId).child('Products').child(product['id']).update({'cantidad': product['cantidad']})
    try:

        ref.child(userId).child("Productos Vendidos").update(sales)
        error = "ok"
        return error
    except (e) as e:
        print(e)
        error = "error"
        return error

@bp.route('/sales/purchased/<userId>', methods=['GET'])
def purchased(userId):
    purchase = ref.child(userId).child('Productos Vendidos').get()
    return purchase

@bp.route('information/<userId>', methods=['POST'])
def saveInformation(userId):
    businessInformation = request.get_json()
    ref.child(userId).child("Business Information").update(businessInformation)
    return 'ok'

@bp.route('/notification', methods=['POST'])
def send_notification_to_device():
    data = request.get_json()
    registration_token = data.get('registration_token')
    title = data.get('title')
    userId = data.get('body')
    if title == 'Activar':
        products = purchased(userId=userId)
        total = 0
        for key in products:
            total += products[key]['metadata']['precio total']
        print(total)
            # Crea un mensaje
        message = messaging.Message(
        notification=messaging.Notification(
            title='Genial! BusinessBot te informa que activaste las notificaciones',
            body='Recibirás un recordatorio de tus ganancias cada 15 días'
        ),
        token=registration_token,
    )
        response = messaging.send(message)
        time.sleep(5)

        message = messaging.Message(
        notification=messaging.Notification(
                title='BusinessBot te informa',
                body='Que tus ganancias hasta el día de hoy son de $'+str(total)+' dólares',
        ),
        token=registration_token,
        )
        # Envia el mensaje
        response = messaging.send(message)
        
    else:
        message = messaging.Message(
        notification=messaging.Notification(
            title='Es una pena',
            body='Ya no recibirás nuestras notificaciones.'
        ),
        token=registration_token, )
    # Envia el mensaje
    response = messaging.send(message)
    return jsonify(response)