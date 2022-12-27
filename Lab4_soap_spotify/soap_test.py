# python -m pip install suds
from spyne import Array
from suds.client import Client
c = Client('http://127.0.0.1:8000/?wsdl')

if __name__ == '__main__':
    # print(c.service.CreateUser("user1", 'password', 'client-administrator'))
    # print(c.service.CreateUser("user2", "password", "client"))
    # print(c.service.CreateUser("admin", "passadmin", "administrator"))
    # print(c.service.CreateUser("user3", "pass", "user"))
    # print(c.service.Login("user1", "password"))
    # print(c.service.Login("user6", "111"))
    # print(c.service.ChangePassword("user3", "pass", "new_password"))
    # print(c.service.DeleteUser("user6"))
    # print(c.service.DeleteUser("user3"))
    # print(c.service.GetUsers())
    print(c.service.GetUserInfoById(40))
    # print(c.service.GetRoles())
    # print(c.service.GetUserInfo("user1"))
    # print(c.service.Authorizate("user1", "administrator"))
