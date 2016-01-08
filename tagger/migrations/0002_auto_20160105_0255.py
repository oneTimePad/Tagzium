# -*- coding: utf-8 -*-
# Generated by Django 1.9 on 2016-01-05 02:55
from __future__ import unicode_literals

from django.conf import settings
import django.core.files.storage
from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('tagger', '0001_initial'),
    ]

    operations = [
        migrations.CreateModel(
            name='Picture',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('photo', models.ImageField(default=0, storage=django.core.files.storage.FileSystemStorage(location='/var/www/html/PHOTOS/'), upload_to='')),
            ],
        ),
        migrations.AlterField(
            model_name='event',
            name='users',
            field=models.ManyToManyField(related_name='events_in', to=settings.AUTH_USER_MODEL),
        ),
        migrations.AddField(
            model_name='picture',
            name='event',
            field=models.ForeignKey(default=0, on_delete=django.db.models.deletion.CASCADE, to='tagger.Event'),
        ),
        migrations.AddField(
            model_name='picture',
            name='users',
            field=models.ManyToManyField(default=0, related_name='pictures_in', to=settings.AUTH_USER_MODEL),
        ),
    ]