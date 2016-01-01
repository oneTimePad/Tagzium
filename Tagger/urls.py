
from django.conf.urls import url,include
from django.contrib import admin
from tagger.views import *
from rest_framework.routers import SimpleRouter,DefaultRouter
authentication = [
    url(r'^signup$',Signup.as_view()),
    url(r'^login$', ObtainExpiringAuthToken.as_view()),
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
