
from django.conf.urls import url,include
from django.contrib import admin
from tagger.views import *
from rest_framework.routers import SimpleRouter,DefaultRouter
from rest_framework_jwt.views import obtain_jwt_token,refresh_jwt_token,verify_jwt_token
authentication = [
    url(r'^signup$',Signup.as_view()),
    url(r'^login$',obtain_jwt_token),
    url(r'^refresh$',refresh_jwt_token),
    url(r'^verify$',verify_jwt_token),
]

urlpatterns = [
    url(r'^admin/', admin.site.urls),
    url(r'^auth/',include(authentication))
]
router = SimpleRouter()
router.register(r'users',UserViewSet,'users')
urlpatterns+=router.urls
router = DefaultRouter()
router.register(r'events',EventViewSet,'events')
urlpatterns+=router.urls
