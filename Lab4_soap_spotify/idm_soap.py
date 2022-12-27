import lxml

from repositories.role_repository import get_roles, create_role
from repositories.user_repository import create_user, login_user, change_password, delete_user, get_user_with_token, get_user_by_id, authorize, get_users, logout

# python -m pip install lxml spyne
from spyne import Application, rpc, ServiceBase, Integer, String, Array, AnyDict, Iterable
from spyne.protocol.soap import Soap11
from spyne.server.wsgi import WsgiApplication
from spyne.model.complex import ComplexModel


# class RoleComplexType(ComplexModel):
#     _type_info = [
#         ('role_name', String)
#     ]
#
#     def __init__(self, role_name):
#         self.role_name = role_name

class UserComplexType(ComplexModel):
    _type_info = [
        ('username', String),
        ('password', String),
        ('roles', Iterable(String)),
        ('error', String)
    ]

    def __init__(self, user:dict):
        self.username = user['username']
        self.password = user['password']
        roles = user['roles'].split(',')
        self.roles = roles

    def set_error(self, message):
        self.error = message

class UsersComplexType(ComplexModel):
    _type_info = [
        ("users", Iterable(UserComplexType)),
        ("error", String)
    ]

    def __init__(self, users):
        self.users = users

    def set_error(self, message):
        self.error = message

class IDMService(ServiceBase):
    @rpc(String, String, String, String, _returns=String)
    def CreateUser(ctx, token, username, password, roles):
        id_user = create_user(token, username, password, roles)
        return str(id_user)

    @rpc(String, String, _returns=String)
    def DeleteUser(ctx, token, username):
        id_user = delete_user(token, username)
        return str(id_user)

    # @cross_origin()
    @rpc(String, String, _returns=String)
    def Login(ctx, username, password):
        print(username)
        print(password)
        login_message = login_user(username, password)
        print(login_message)

        return str(login_message)

    @rpc(String, String, String, String, _returns=String)
    def ChangePassword(ctx, token, username, old_password, new_password):
        password_message = change_password(token, username, old_password, new_password)
        return str(password_message)

    @rpc(String, _returns=String)
    def CreateRole(ctx, role_name):
        new_role = create_role(role_name)
        return new_role

    @rpc(String, _returns=UsersComplexType)
    def GetUsers(ctx, token):
        if token:
            users = get_users(token)
            complexUsers = []
            if type(users) != type("string"):
                for user in users:
                    print(user)
                    complexUsers.append(UserComplexType(user))
                return UsersComplexType(complexUsers)

            complexUsers = UsersComplexType([])
            complexUsers.set_error(users)
            return complexUsers

    @rpc(String, String, _returns=String)
    def GetUserInfo(ctx, token, username):
        user_info = get_user_with_token(token, username)
        # return UserComplexType(user_info)#.getInfo()
        return str(user_info)

    @rpc(_returns=String)
    def GetRoles(ctx):
        roles = get_roles()
        return roles

    @rpc(String, _returns=String)
    def Authorize(ctx, token):
        print(token)
        result = authorize(token)
        return str(result)

    @rpc(String, _returns=String)
    def Logout(ctx, token):
        result = logout(token)
        return str(result)

    # def on_method_return_object(ctx):
    #     ctx.transport.resp_header['Acces-Control-Allow-Origin'] = ctx.descriptor.service_clas_origin

application = Application([IDMService], 'services.spotify.idm.soap',
                          in_protocol=Soap11(validator='lxml'),
                          out_protocol=Soap11())
wsgi_application = WsgiApplication(application)


if __name__ == "__main__":
    import logging

    from wsgiref.simple_server import make_server

    logging.basicConfig(level=logging.INFO)
    logging.getLogger('spyne.protocol.xml').setLevel(logging.INFO)

    logging.info("listening to http://127.0.0.1:8000")
    logging.info("wsdl is at: http://127.0.0.1:8000/?wsdl")

    server = make_server('127.0.0.1', 8000, wsgi_application)
    server.serve_forever()